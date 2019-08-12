package org.example.basicapp;

import java.io.File;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class TestS3Bucket {

	public static void main(String[] args) {
		
		String s3Bucket = "revature-jose-test-bucket";
		String fileName = "Dockerfile";
		
		S3Client s3Client = S3Client.builder()
				.build();
		
		File file = new File(TestS3Bucket.class.getClassLoader().getResource(fileName).getFile());
		
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
		        .bucket(s3Bucket).key(file.getName())
		        .acl(ObjectCannedACL.PUBLIC_READ).build();

		    PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
		    
		    System.out.println("https://revature-jose-test-bucket.s3.us-east-2.amazonaws.com/Dockerfile");
	}
}
