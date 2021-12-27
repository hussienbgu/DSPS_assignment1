

import java.util.List;
import java.io.*;
import com.google.gson.Gson;
import software.amazon.awssdk.services.sqs.model.Message;

public class Worker {
    static sentimentAnalysisHandler sa = new sentimentAnalysisHandler();
    static namedEntityRecognitionHandler nr = new namedEntityRecognitionHandler() ;
    public static final String MangerTOWorkers = "mangertoworkersq";
    public static String htmlIt(String review,String https, String rating)
    {
        String reply_entities = nr.printEntities(review);
        String Color = rating.equals("1")? "Dark": rating.equals("2")? "Red" :
                rating.equals("3")? "Black": rating.equals("4")? "LightGreen" : "DarkGreen";
        String reply_sarcasm = "Is sarcastic? "+(Math.abs(Integer.parseInt(rating)-sa.findSentiment(review))>3?"Yes.":"No.");
        return rating+"\n"+https+"\n"+reply_entities+"\n"+reply_sarcasm+"\n";
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        String sqsJsonStrMsg;
        Msg sqsJsonObjMsg;
        Review rev;
        Thread keepMessage = null;
        KeepMassageAlive alive ;
        while (true) {
            List<Message> messages = SQSCC.receiveMessages(MangerTOWorkers,1);
            if (messages != null && messages.size() != 0) {
                try {
                    System.out.println(messages.get(0).body());
                    alive = new KeepMassageAlive(messages.get(0), MangerTOWorkers);
                    keepMessage = new Thread(alive);
                    keepMessage.start();
                    sqsJsonStrMsg = messages.get(0).body();
                    sqsJsonObjMsg = new Gson().fromJson(sqsJsonStrMsg, Msg.class);
                    rev = new Gson().fromJson(sqsJsonObjMsg.body, Review.class);
                    String answer = htmlIt(rev.getText(), rev.getLink(), rev.getRating());
                    Msg reply = new Msg(false, answer, "");
                    SQSCC.sendmsg(SQSCC.geturl(sqsJsonObjMsg.sqsreply),reply);
                    alive.terminate = true;
                }
                catch (Exception e) {
                    System.out.println("OOPS");
                    if (keepMessage!=null)
                        keepMessage.interrupt();
                }
            }
            else {
                Thread.sleep(5*1000);
            }
        }
    }
}