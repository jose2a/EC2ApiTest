package org.example.basicapp;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

public class GetDnsTest {

	public static void main(String[] args) {
		
		String instance_id = "i-0925ba8306131e42e";
		
		// Ec2 client
		Ec2Client ec2 = Ec2Client.create();
		
		String nextToken = null;
        do {
            DescribeInstancesRequest desRequest = DescribeInstancesRequest.builder()
            		.nextToken(nextToken).build();
            
            DescribeInstancesResponse desResponse = ec2.describeInstances(desRequest);

            for (Reservation reservation : desResponse.reservations()) {
                for (Instance instance : reservation.instances()) {
                	if (instance.instanceId().equals(instance_id)) {
						
                		System.out.printf(
                				"Found reservation with id %s, " +
                						"type %s, " +
                						"state %s " +
                						"and dns %s",
                						instance.instanceId(),
                						instance.instanceType(),
                						instance.state().name(),
                						instance.publicDnsName());
					}
                }
            }
            nextToken = desResponse.nextToken();


        } while (nextToken != null);

	}

}
