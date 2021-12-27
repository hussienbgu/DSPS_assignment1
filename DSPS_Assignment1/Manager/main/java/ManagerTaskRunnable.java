import com.google.gson.Gson;
import software.amazon.awssdk.services.sqs.model.Message;
import java.util.List;

public class ManagerTaskRunnable implements Runnable{

    public volatile static int numOfCurrentWorkers = 1;
    public static final String MangerTOWorkers = "mangertoworkersq";
    public static final String WorkerToManger = "workertomangerq";
    final Msg message;
    final String replyQueue;
    final double random = Math.random();
    public ManagerTaskRunnable(Msg message)
    {
        this.message=message;
        this.replyQueue = message.sqsreply;
    }

    public void sendTasksToWorkers(Review review){
        Msg managerToWorkerMessage = new Msg(false,
                new Gson().toJson(review),
                WorkerToManger+Thread.currentThread().getId()+((int)(random*1000)));
        SQSCC.sendmsg(SQSCC.geturl(MangerTOWorkers),managerToWorkerMessage);
    }
    @Override
    public void run() {

        int numOfMessages = 0;
        int curr = 0;
        SQSCC.createSqs(WorkerToManger+Thread.currentThread().getId()+((int)(random*1000)));
        List<Message> messageList;
        String[] lines;
        Product product;
        Msg sqsMessage;
        StringBuilder stringBuilder = new StringBuilder();
        String s3Content = S3CC.getObject("input.txt",message.Bucketname);
        lines = s3Content.split("\n");
        System.out.println("Sending the products to the workers");
        for (String line_ : lines)
        {
            product = new Gson().fromJson(line_,Product.class);
            for (Review review_ : product.reviews)
            {
                sendTasksToWorkers(review_);
                numOfMessages++;
            }

        }
        int newWorkers = numOfMessages/message.n;
        createMoreInstances(newWorkers);
        //RECEIVING THE REPLIES FROM WORKERS
        stringBuilder = new StringBuilder();
        while (curr < numOfMessages)
        {
            messageList = SQSCC.receivemsg(WorkerToManger+Thread.currentThread().getId()+((int)(random*1000)));
            if (messageList.size()!=0)
            {
                for (Message message_ : messageList)
                {
                    System.out.println("received a reply from worker");
                    sqsMessage = new Gson().fromJson(message_.body(),Msg.class);
                    stringBuilder.append(sqsMessage.body);
                    curr++;
                    SQSCC.deletemsg(SQSCC.geturl(WorkerToManger+Thread.currentThread().getId()+((int)(random*1000))),message_);
                }
            }
            try {
                if (messageList.size()==0) {
                    Thread.sleep(10 * 1000);
                }
                else {
                    System.out.println("Wait worker");
                }
            }
            catch (Exception e)
            {
                System.out.println(e);
            }

        }
        //UPLOADING THE S3 FILE
        System.out.println("Uploading to local a whole file to bucket: " + message.Bucketname);
        S3CC.putObject(stringBuilder.toString(),
                "output.txt", message.Bucketname);
        S3CC.deleteObject("input.txt",message.body);
        //SENDING SQS MESSAGE TO INFORM WE ARE DONE

        System.out.println("Sending message to tell the local manager is done");
        Msg newmsg = new Msg(false,"DONE","");
        SQSCC.sendmsg(SQSCC.geturl(message.sqsreply),newmsg);
        SQSCC.deletequeue(SQSCC.geturl(WorkerToManger+Thread.currentThread().getId()+((int)(random*1000))));
    }


    private synchronized static void  createMoreInstances(int numOfWantedWorkers) {
        if (numOfWantedWorkers == 0) return;
        if (numOfWantedWorkers > numOfCurrentWorkers) {
            EC2CC.runEc2("Worker",Manager.initWorker, numOfWantedWorkers - numOfCurrentWorkers);
            numOfCurrentWorkers = numOfWantedWorkers;
        }
    }
}