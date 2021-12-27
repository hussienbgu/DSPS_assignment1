import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import software.amazon.awssdk.services.sqs.model.Message;


public class Manager {
    public static String initInstanceBash =
            "#!/bin/bash\n" +
                    "sudo mkdir /root/.aws\n" +
                    "sudo yum update -y\n" +
                    "wget https://hussshqere.s3.amazonaws.com/AppHussien-1.0-SNAPSHOT.jar\n"+
                    "sudo yum install java-1.8.0 -y\n" +
                    "echo \"" + loadCredentials() + "\" > /root/.aws/credentials" + "\n";
    public static String initWorker = initInstanceBash + "java -jar /AppHussien-1.0-SNAPSHOT.jar Worker\n";
    public static boolean terminate = false;
    public static final String LocalToManger = "localtomangerq";
    public static final String MangerTOWorkers = "mangertoworkersq";
    public static final String Bucket = "bucket-1998";
    public static void main(String args[]) throws IOException, InterruptedException {
        System.out.println("*      Manager Started        *");
        ExecutorService pool = Executors.newCachedThreadPool(Thread::new);
        SQSCC.createSqs(MangerTOWorkers);
        EC2CC.runEc2("Worker",initWorker, 1);
        while (true) {
            List<Message> messages;
            if (terminate) {
                messages = SQSCC.receivemsg((LocalToManger));
                pool.shutdown();
                while (!pool.isTerminated()) {
                    ;
                }
                deleteandterminate();
                break;

            }
            messages = SQSCC.receivemsg((LocalToManger));
            if (messages != null && messages.size() != 0) {
                Msg sqsmessage = new Gson().fromJson(messages.get(0).body(), Msg.class);
                terminate = sqsmessage.terminate;
                ManagerTaskRunnable managerTaskRunnable = new ManagerTaskRunnable(sqsmessage);
                SQSCC.deletemsg(SQSCC.geturl(LocalToManger),messages.get(0));
                pool.execute(managerTaskRunnable);
            }
        }
    }
    private static void deleteandterminate() {
        SQSCC.deletequeue(SQSCC.geturl(LocalToManger));
        SQSCC.deletequeue(SQSCC.geturl(MangerTOWorkers));
        EC2CC.terminateAll("Worker");
        EC2CC.terminateAll("Manager");
    }
    public static String loadCredentials() {
        BufferedReader bufferedReader;

        try {
            String path = System.getProperty("user.home") + File.separator + "/.aws/credentials";
            File customDir = new File(path);
            bufferedReader = new BufferedReader(new FileReader(customDir));
            StringBuilder ret = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ret.append(line).append("\n");
            }
            return ret.toString();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }
}
