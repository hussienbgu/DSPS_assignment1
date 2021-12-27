
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class main {
    public static void main (String[] args) throws IOException, InterruptedException {
        if (args == null || args.length == 0) {
            System.out.println("NO Args !!!!");
            return;
        }
        //Creating credentials from file
        String path = System.getProperty("user.home") + File.separator + ".aws/credentials";
        Credentials.setAll(path);
        switch (args[0]) {
            case "Manager":
                Manager.main(new String[]{});
                break;
            case "Worker":
                Worker.main(new String[]{});
                break;
            case "Local":
                LocalApp.main(Arrays.copyOfRange(args, 1, args.length));
                break;
        }
    }
}