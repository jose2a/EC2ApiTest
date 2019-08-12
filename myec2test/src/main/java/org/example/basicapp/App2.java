package org.example.basicapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Scanner;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Tag;

/**
 * NO USED!!!!
 *
 */
public class App2 {
	public static void main(String[] args) throws UnsupportedEncodingException {
		
		String sqlFileName = "trmsv2-insert.sql";
		
		Scanner sc = null;
		String sqlFileContent = "";

		try {
			File file = new File(Test2.class.getClassLoader().getResource(sqlFileName).getFile());
			sc = new Scanner(file);

			while (sc.hasNextLine()) {
				sqlFileContent += sc.nextLine() + "\n";
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			sc.close();
		}
		
		sc = null;
		String dockerFile = "";

		try {
			File file = new File(Test2.class.getClassLoader().getResource("Dockerfile").getFile());
			sc = new Scanner(file);

			while (sc.hasNextLine()) {
				dockerFile += sc.nextLine() + "\n";
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			sc.close();
		}
		
		//=====================================================================
		
		String gitUrl = "https://github.com/miguno/java-docker-build-tutorial";
		String[] urlPieces = gitUrl.split("/");
		
		String repoName = urlPieces[urlPieces.length - 1];
		
		//=====================================================================
		
		String bashScript = 
				"#!/bin/bash\n"
				+ "sudo yum update -y\n"
				+ "sudo yum install -y docker\n"
				+ "sudo service docker start\n"
				+ "sudo yum install -y git\n"
				+ "cd /tmp\n"
				+ "sudo git clone " + gitUrl + ".git\n"
				+ "cd " + repoName + "/\n"
				+ "sudo docker build -t " + repoName + " .\n"
				+ "sudo docker run -p 80:8123 -d " + repoName + ":latest\n"
				+ "cd /tmp\n"
				+ "sudo mkdir db\n"
				+ "cd db\n"
				+ "sudo touch Dockerfile\n"
				+ "sudo chmod ugo+rwx Dockerfile\n"
				+ "sudo cat > Dockerfile << EOF\n"
				+ dockerFile
				+ "EOF\n"
				+ "sudo docker build --rm=true --build-arg DB_NAME=trms --build-arg SQL_FILE=script.sql -t postgresql:9.3 .\n"
				+ "sudo docker run -i -t -p 5432:5432 -d postgresql:9.3\n";
		
		System.out.println(bashScript);

		String name = "Revature test";
		String ami_id = "ami-02f706d959cedf892";
		String groupName = "revature";
		String key_name = "revatureRPM";

		// Ec2 client
		Ec2Client ec2 = Ec2Client.create();
		
		//=====================================================================
		
		// Security Group
		CreateSecurityGroupRequest create_request = CreateSecurityGroupRequest.builder()
				.groupName(groupName)
				.description("Revature tests")
				.vpcId("vpc-0913f361")
				.build();
		
		try {
			// Creating Security Group
			ec2.createSecurityGroup(create_request);
		} catch (Exception e) {
			System.out.println("Security group already exist.");
		}
		
		//=====================================================================

        // Creating rules to access the ec2 instance from outside
        IpRange ip_range = IpRange.builder()
            .cidrIp("0.0.0.0/0").build();

        IpPermission ip_perm = IpPermission.builder()
            .ipProtocol("tcp")
            .toPort(80)
            .fromPort(80)
            .ipRanges(ip_range)
            .build();

        IpPermission ip_perm2 = IpPermission.builder()
            .ipProtocol("tcp")
            .toPort(22)
            .fromPort(22)
            .ipRanges(ip_range)
            .build();
        
        IpPermission ip_perm3 = IpPermission.builder()
                .ipProtocol("tcp")
                .toPort(5432)
                .fromPort(5432)
                .ipRanges(ip_range)
                .build();

        AuthorizeSecurityGroupIngressRequest auth_request = AuthorizeSecurityGroupIngressRequest.builder()
                .groupName(groupName)
                .ipPermissions(ip_perm, ip_perm2, ip_perm3)
                .build();

        try {
        	// Creating rule for the ec2 instance
			ec2.authorizeSecurityGroupIngress(auth_request);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Rule already exist.");
		}
        
        //=====================================================================
		
		CreateKeyPairRequest createKeyPairRequest =  CreateKeyPairRequest.builder()
				.keyName(key_name).build();
		
		
		try {
			
			CreateKeyPairResponse createKeyPairResponse = ec2.createKeyPair(createKeyPairRequest);

			try (PrintStream out = new PrintStream(new FileOutputStream(key_name + ".pem"))) {
				// Saving the .pem file in case we need to login the system using ssh 
				out.print(createKeyPairResponse.keyMaterial());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				System.out.println("Exception when saving the file.");
			}
		} catch (Exception e) {
			System.out.println("Pair response already exist.");
		}
		
		//=====================================================================
		
		// Create instance in ec2
		RunInstancesRequest run_request = RunInstancesRequest.builder()
				.imageId(ami_id)
				.instanceType(InstanceType.T2_MICRO)
				.maxCount(1)
				.minCount(1)
				.keyName(createKeyPairRequest.keyName())
				.securityGroups(create_request.groupName())
				.userData(Base64.getEncoder().encodeToString(bashScript.getBytes("UTF-8")))
				.build();

		// Run instance
		RunInstancesResponse response = ec2.runInstances(run_request);

		String instance_id = response.instances().get(0).instanceId();

		Tag tag = Tag.builder().key("Name").value(name).build();

		CreateTagsRequest tag_request = CreateTagsRequest.builder()
				.resources(instance_id)
				.tags(tag)
				.build();

		try {
			ec2.createTags(tag_request);

	        // snippet-start:[ec2.java2.describe_instances.main]
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
	        // snippet-end:[ec2.java2.describe_instances.main]
		} catch (Ec2Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		// snippet-end:[ec2.java2.create_instance.main]
		System.out.println("Done!");

	}
}