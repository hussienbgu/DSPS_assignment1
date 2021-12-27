

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;


import java.util.Base64;

public class EC2CC {

    public static final String amiId = "ami-0742b4e673072066f";

    public static Ec2Client ec2 = Ec2Client.builder().region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(Credentials.getCredentials()))
            .build();

    // create a new instance
    public static void runEc2(String name, String userData, int amount) {
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .instanceType(name.equals("worker") ? InstanceType.T2_LARGE : InstanceType.T2_LARGE)
                .imageId(amiId)
                .maxCount(amount)
                .minCount(amount)
                .userData(Base64.getEncoder().encodeToString(userData.getBytes()))
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);

        String instanceId = response.instances().get(0).instanceId();

        Tag tag = Tag.builder()
                .key("name")
                .value(name)
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagRequest);
            System.out.printf(
                    "Successfully started EC2 instance %s based on AMI %s",
                    instanceId, amiId);

        } catch (Ec2Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done!");
        return;
    }

    // create an instance if no exist
    public static void createifnotex(String name, String userdata) {
        {
            String nextToken = null;
            boolean flag = false;
            try {
                do {
                    DescribeInstancesRequest request = DescribeInstancesRequest.builder().nextToken(nextToken)
                            .build();
                    DescribeInstancesResponse response = ec2.describeInstances(request);
                    for (Reservation reservation : response.reservations()) {
                        for (Instance instance : reservation.instances()) {
                            if (!instance.state().name().toString().equals("terminated") && instance.tags().get(0).value().equals("Manager")) {
                                flag = true;
                            }
                        }
                    }
                    nextToken = response.nextToken();
                } while (nextToken != null);

            } catch (Exception f) {
                System.out.println(f);
                System.out.println("startIfNotExistFails");
                System.exit(-1);
            }
            if (!flag)
                runEc2(name, userdata, 1);
        }
    }

    //terminate All the instances with the given name
    public static void terminateAll(String name){
        {
            String nextToken = null;
            try {
                do {
                    DescribeInstancesRequest request = DescribeInstancesRequest.builder().nextToken(nextToken)
                            .build();
                    DescribeInstancesResponse response = ec2.describeInstances(request);
                    for (Reservation reservation : response.reservations()) {
                        for (Instance instance : reservation.instances()) {
                            if (instance.tags().get(0).value().equals(name)){
                                ec2.terminateInstances(TerminateInstancesRequest.builder().instanceIds(instance.instanceId()).build());
                                System.out.println("terminated!!");
                            }
                        }
                    }
                    nextToken = response.nextToken();
                } while (nextToken != null);

            } catch (Exception f) {
                System.out.println(f);
                System.exit(-1);
            }
        }
    }

    // terminate instance with given instance ID
    public static void terminate(String instanceId) {
        TerminateInstancesRequest terminate_request = TerminateInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();
        ec2.terminateInstances(terminate_request);
    }

}

