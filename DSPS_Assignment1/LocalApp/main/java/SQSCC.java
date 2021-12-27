
import com.google.gson.Gson;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.List;

public class SQSCC {

    public static SqsClient sqs = SqsClient.builder().region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.
                    create(Credentials.getCredentials())).build();

    // create new Sqs
    public static void createSqs(String Qname) {
        try {
            CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName(Qname)
                    .build();
            CreateQueueResponse create_result = sqs.createQueue(request);

        } catch (QueueNameExistsException e) {
            return;
        }
    }

    // get url of sqs
    public static String geturl(String Qname) {
        String queue_url = "";
        try {
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(Qname)
                    .build();
            queue_url = sqs.getQueueUrl(getQueueRequest).queueUrl();
            return queue_url;
        } catch (Exception e) {
            System.out.println("exeption " + e.getMessage());
        }

        return queue_url;
    }

    // Send messages to the queue
    public static void sendmsg(String qUrl, Msg m) {
        String msg = new Gson().toJson(m);
        SendMessageRequest send_msg_request = SendMessageRequest.builder()
                .queueUrl(qUrl)
                .messageBody(msg)
                .delaySeconds(5)
                .build();
        sqs.sendMessage(send_msg_request);
    }

    // receive List of messages from the queue
    public static List<Message> receivemsg(String qUrl) {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(geturl(qUrl))
                .build();
        List<Message> messages = sqs.receiveMessage(receiveRequest).messages();
        return messages;
    }

    // delete messages from the queue
    public static void deletemsg(String qUrl, Message m) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(qUrl)
                .receiptHandle(m.receiptHandle())
                .build();
        sqs.deleteMessage(deleteRequest);
    }

    // delete queue
    public static void deletequeue(String qurl) {

        DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder().queueUrl(qurl).build();
        sqs.deleteQueue(deleteQueueRequest);
    }

    public static void changeVisibilityTime(String queueUrl, Message msg, Integer newVisibilityTimeout) {

        sqs.changeMessageVisibility(ChangeMessageVisibilityRequest.builder().
                visibilityTimeout(newVisibilityTimeout).queueUrl(queueUrl).
                receiptHandle(msg.receiptHandle()).build());

    }
    public static List<Message> receiveMessages(String sqsName, int max)
    {
        return sqs.receiveMessage(ReceiveMessageRequest.builder().queueUrl(
                sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsName).build()).queueUrl()
        ).maxNumberOfMessages(max)
                .build()).messages();
    }
}

