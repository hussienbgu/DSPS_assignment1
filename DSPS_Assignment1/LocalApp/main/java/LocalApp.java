
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.*;
import java.util.List;



public class LocalApp
{
    public static boolean Terminate = false;
    public static int n;
    public static File [] input_files  ;
    public static File [] output_files ;
    public static int num_of_files  ;
    public static int curr_file = 0 ;
    public static final String mangerToLocalr = "mangertolocalq"+(int)(Math.random()*10000);
    public static final String LocalToManger = "localtomangerq";
    public static final String Bucket = "bucket-1998"+(int)(Math.random()*10000);


    public static void main( String[] args ) throws IOException {
        parse_args(args);
        checkManagerAndLaunch();
    }

    private static void  parse_args(String []args) {
        System.out.println(" ***************** Reading local arguments *****************");
        int number_of_input_files;
        if(args.length==0) {
            System.err.println("...................No Args.............");
            throw new RuntimeException();

        }
        if(args[args.length - 1].equals("terminate")) {
            Terminate = true;
            n = Integer.parseInt(args[args.length-2]);
            number_of_input_files = (args.length -2)/2;
            num_of_files = number_of_input_files;
        }
        else {
            number_of_input_files  =(args.length -1)/2;
            num_of_files = number_of_input_files;
            n = Integer.parseInt(args[args.length-1]);
        }
        getFiles(args,number_of_input_files);
        System.out.print("args : n: "+ n + " Terminate :  ");
        System.out.print(Terminate);
        System.out.println("  num of input files  : "+ input_files.length);
        System.out.println(".................done...............");


    }

    private static void getFiles(String[] args , int num_of_files){
        input_files = new File[num_of_files ];
        output_files = new File[num_of_files ];
        for (int i = 0 ; i <num_of_files;i++) {
            input_files[i] = new File(args[i]);
            output_files[i] = new File(args[num_of_files + i]);
        }
    }

    private static void checkManagerAndLaunch() throws IOException {
        System.out.print(" Running Manager !! \n");
        init();
        // uploads Files to the bucket and send the msg to the manger
        // and wait to recive a msg from the manger to know that the job is done
        for (int i = 0 ; i < num_of_files ; i++) {
            curr_file = i;
            S3CC.uploadfileToS3(input_files[i].toPath(),"input.txt",Bucket);
            String q_url_local_to_manger  = SQSCC.geturl(LocalToManger);
            boolean safe =  (i + 1 == num_of_files);
            boolean safeterminate = Terminate && safe ;
            Msg to_send = new Msg(safeterminate,Bucket,mangerToLocalr);
            to_send.n = n;
            to_send.Bucketname = Bucket;
            SQSCC.sendmsg(q_url_local_to_manger,to_send);
            while (true){
                // wait to receive a msg from the manger
                //System.out.printf(mangerToLocalr);
                List<Message> msgs = SQSCC.receivemsg((mangerToLocalr));
                if (!msgs.isEmpty()){
                    // Delete the msg from the reply q
                    SQSCC.deletemsg(SQSCC.geturl(mangerToLocalr),msgs.get(0));
                    // get the Object from the Bucket
                    ResponseInputStream response = S3CC.getObject1(Bucket,"output.txt");
                    // make html
                    makeHtmlfile(response);
                    // delete the file from the bucket in s3
                    S3CC.deleteObject("output.txt",Bucket);
                    break;
                }
            }
        }
        SQSCC.deletequeue(SQSCC.geturl(mangerToLocalr));
        S3CC.deleteBucket(Bucket);
    }

    private static void makeHtmlfile(ResponseInputStream response) throws IOException {
        final String Colors [] = {"darkred","red","black","lightgreen","darkgreen"};
        InputStreamReader reader = new InputStreamReader(response);
        BufferedReader bufferreader = new BufferedReader(reader);
        FileOutputStream fos = new FileOutputStream(output_files[curr_file]);
        PrintWriter writer = new PrintWriter(fos);
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<body>");
        String nextline = bufferreader.readLine();
        while (nextline!=null){
            String color = Colors[Integer.parseInt(nextline)-1];
            String link = bufferreader.readLine();
            String entities = bufferreader.readLine();
            String sarcasm = bufferreader.readLine();
            String htmlformat = makeHtml(color,sarcasm,link,entities);
            writer.println(htmlformat);
            nextline = bufferreader.readLine();
        }
        writer.println("</body>");
        writer.println("</html>");
        response.close();
        reader.close();
        writer.close();
    }

    private static void init() {
        // if there's no manager, create one and run it
        EC2CC.createifnotex("Manager",intManager);
        // check if the bucket exists, if not create new one.
        if (!S3CC.checkBucketExists(Bucket))
            S3CC.createBucket(Bucket);
        // create SQS to send msg from local to the manger
        SQSCC.createSqs(LocalToManger);
        // create SQS to get reply from manger
        SQSCC.createSqs(mangerToLocalr);
        try {
            Thread.sleep(2*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String makeHtml(String color ,String sarcasm,String link ,String entities){
        return ("<p style=color:"+ color +  ";>"+
                link + "</p>\n" + "<p> " + entities + "</p>" + "\n" +
                "<p> " +  sarcasm + "</p>" + "\n");

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

    public static String initInstanceBash =
            "#!/bin/bash\n" +
                    "sudo mkdir /root/.aws\n" +
                    "sudo yum update -y\n" +
                    "wget https://hussshqere.s3.amazonaws.com/AppHussien-1.0-SNAPSHOT.jar\n"+
                    "sudo yum install java-1.8.0 -y\n" +
                    "echo \"" + loadCredentials() + "\" > /root/.aws/credentials" + "\n";
    public static String intManager = initInstanceBash + "java -jar /AppHussien-1.0-SNAPSHOT.jar Manager\n";


}