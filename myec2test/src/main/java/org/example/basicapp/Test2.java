package org.example.basicapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * NOT USED!!!!
 * @author jose2a
 *
 */
public class Test2 {

	public static void main(String[] args) throws FileNotFoundException {
		String gitUrl = "https://github.com/miguno/java-docker-build-tutorial";

		String[] urlPieces = gitUrl.split("/");

		System.out.println(urlPieces[urlPieces.length - 1]);

//		Scanner sc = null;
//		String dockerFile = "";
//
//		try {
//			File file = new File(Test2.class.getClassLoader().getResource("Dockerfile").getFile());
//			sc = new Scanner(file);
//
//			while (sc.hasNextLine()) {
//				dockerFile += sc.nextLine() + "\n";
//			}
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		} finally {
//			sc.close();
//		}
//		
//		System.out.println(dockerFile);
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
		
		System.out.println(sqlFileContent);

	}
}