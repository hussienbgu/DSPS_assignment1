import com.google.gson.Gson;

public class Msg {
    //public int numOfMessagesPerWorker;
    public int n = 0;
    public boolean terminate;
    public String fileinS3;
    public String Bucketname;
    public String sqsreply;
    public String body;


    public Msg(boolean terminate, String body, String sqsreply) {
        this.sqsreply = sqsreply;
        this.terminate = terminate;
        this.body = body;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}

