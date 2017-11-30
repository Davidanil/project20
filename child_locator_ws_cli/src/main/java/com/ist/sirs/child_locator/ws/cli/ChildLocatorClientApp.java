package com.ist.sirs.child_locator.ws.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChildLocatorClientApp {
	public static ChildLocatorClient client = null;

	public static void main(String args[]) {

		String wsURL = args[0];

		// Create client
		client = new ChildLocatorClient(wsURL);

		// System.out.println(client.print());
		// System.out.println(client.login("sheldon","bazinga"));

		// check if its the first time user executes app
		if (!(new File("src/main/resources/pin")).isFile())
			createPin();
		else
			checkPin();

	}

	public static void createPin() {
		Scanner scanner = new Scanner(System.in);
		String pin;

		clearScreen();
		System.out.println("Welcome to Child Locator");
		System.out.println("Since its the first time you execute this app, choose a 4-8 digit Pin:");

		while (true) {
			try {
				System.out.print("Pin: ");
				pin = scanner.next();

				// check if valid
				Pattern pattern = Pattern.compile("^\\d{4,8}$");
				Matcher matcher = pattern.matcher(pin);
				if (matcher.find()) {
					// hash pin and save it
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));

					FileOutputStream out = new FileOutputStream("src/main/resources/pin");
					out.write(hash);
					out.close();
					
					firstMenuViewInterface();
					return;
				} else
					System.out.println("Invalid pin, try again:");
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			} finally {
			}
		}
	}

	public static void checkPin() {
		Scanner scanner = new Scanner(System.in);
		String pin;

		clearScreen();
		System.out.println("Welcome to Child Locator");
		System.out.println("Insert your Pin:");

		while (true) {
			try {
				System.out.print("Pin: ");
				pin = scanner.next();

				// hash pin and compare it
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
					
					File file = new File("src/main/resources/pin");
					//init array with file length
					byte[] hashPinFile = new byte[(int) file.length()];

					FileInputStream fis = new FileInputStream(file);
					fis.read(hashPinFile); //read file into bytes[]
					fis.close();
					
					if(Arrays.equals(hash,hashPinFile)){
						firstMenuViewInterface();
						return;
					}
					else{
						System.out.println("Pin incorrect, try again.");
					}

				
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			} finally {
			}
		}
	}

	public static void firstMenuViewInterface() {
		Scanner scanner;
		String option;

		clearScreen();
		System.out.println("What do you want to do?");

		while (true) {
			scanner = new Scanner(System.in);

			System.out.println("\t1 - Login");
			System.out.println("\t2 - Register");
			System.out.print("Number of the option: ");

			try {
				option = scanner.next();

				switch (option) {
				case "1":
					loginViewInterface();
					return;
				case "2":
					registerViewInterface();
					return;
				default:
					System.out.println("Invalid option, try again:");
					break;
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			} finally {
				scanner.close();
			}
		}

	}

	public static void loginViewInterface() {
		Scanner scanner;
		String username, password;

		clearScreen();
		System.out.println("Provide your info:");

		while (true) {
			scanner = new Scanner(System.in);
			try {
				System.out.print("Username: ");
				username = scanner.next();
				System.out.print("Password: ");
				password = scanner.next();

				// TODO: check if user can login (e^c)
				if (client.login(username, password)) {
					mainViewInterface();
					return;
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			} finally {
				System.out.println("Invalid login, try again:");
				scanner.close();
			}
		}
	}

	public static void registerViewInterface() {
		Scanner scanner;
		String phone, email, password;

		clearScreen();
		System.out.println("Provide your info:");

		while (true) {
			scanner = new Scanner(System.in);

			try {
				System.out.print("Phone: ");
				phone = scanner.next();
				System.out.print("Username: ");
				email = scanner.next();
				System.out.print("Password: ");
				password = scanner.next();

				// TODO: Check if register was successful
				// if (client.register(phone,email, password)) {
				// scanner.close();
				// mainViewInterface();
				// return;
				// }
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			} finally {
				scanner.close();
			}
		}
	}

	public static void mainViewInterface() {
		Scanner scanner;
		String option;

		System.out.println("What do you want to do?");

		while (true) {
			scanner = new Scanner(System.in);

			System.out.println("\t1 - Check followees");
			System.out.println("\t2 - Check followers");
			System.out.println("\t3 - Add new follower");
			System.out.print("Number of the option:");

			try {
				option = scanner.next();

				switch (option) {
				case "1":
					checkFollowees();
					return;
				case "2":
					checkFollowers();
					return;
				case "3":
					addFollower();
					return;
				default:
					System.out.println("Invalid login, try again:");
					break;
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			} finally {
				scanner.close();
			}
		}
	}

	public static void checkFollowees() {

	}

	public static void checkFollowers() {

	}

	public static void addFollower() {

	}

	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}
