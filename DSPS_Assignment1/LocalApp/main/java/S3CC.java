
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class S3CC {
    public static final S3Client s3 = S3Client.builder().region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(Credentials.getCredentials())).build();
    //create New Bucket
    public static void createBucket(String BucketName) {
        s3.createBucket(CreateBucketRequest
                .builder()
                .bucket(BucketName)
                .createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .build())
                .build());

        System.out.println(BucketName + " Bucket created !!!! \n");
    }
    public static boolean checkBucketExists(String bucket){
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucket)
                .build();

        try {
            s3.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }


    }
    // upload file to the Bucket
    public static void uploadfileToS3(Path path, String key , String bucketName)  {
        s3.putObject(PutObjectRequest.builder().key(key).bucket(bucketName).build(),
                RequestBody.fromFile(path));
        System.out.println(key + " File uploaded !!! \n");
    }
    // get object
    public static ResponseInputStream getObject1(String bucket, String key){
        return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
    }
    public static void deleteObject(String key, String bucketName) {
        s3.deleteObject(DeleteObjectRequest.builder().key(key).bucket(bucketName).build());
    }
    // Delete Bucket
    public static void deleteBucket(String bucket) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
        s3.deleteBucket(deleteBucketRequest);
    }
    public static String getObject(String key, String bucketName)
    {
        BufferedReader breader;
        ResponseInputStream<GetObjectResponse> s3Obj =
                s3.getObject(GetObjectRequest.builder().key(key).bucket(bucketName).build());
        breader = new BufferedReader(new InputStreamReader(s3Obj));
        String line;
        StringBuilder ret = new StringBuilder();
        try {

            while ((line = breader.readLine()) != null) {
                ret.append(line).append("\n");
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return ret.toString();
    }
    public static void putObject(String data, String key, String bucketName)
    {
        s3.putObject(PutObjectRequest.builder().key(key).bucket(bucketName).build()
                , RequestBody.fromBytes(data.getBytes(StandardCharsets.UTF_8)));
    }
}