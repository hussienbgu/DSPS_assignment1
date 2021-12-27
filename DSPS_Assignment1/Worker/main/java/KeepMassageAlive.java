
import software.amazon.awssdk.services.sqs.model.Message;

public class KeepMassageAlive implements Runnable{
    private Message message;
    public volatile boolean terminate = false;
    private String queueName;
    public KeepMassageAlive(Message message, String queueName) {
        this.queueName = queueName;
        this.message = message;
    }
    @Override
    public void run() {
        while (true) {
            if (terminate) {
                SQSCC.deletemsg(SQSCC.geturl(queueName),message);
                break;
            }
            else {
                extendMessageTime(this.message,SQSCC.geturl(queueName), 15);
            }
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e)
            {
                System.out.println(e);
            }
        }

    }
    public static void extendMessageTime(Message msg, String queueUrl, Integer newVisibilityTimeout) {
        SQSCC.changeVisibilityTime(queueUrl, msg, newVisibilityTimeout);
    }
}