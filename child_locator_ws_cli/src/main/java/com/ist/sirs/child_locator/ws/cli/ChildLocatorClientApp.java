package com.ist.sirs.child_locator.ws.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ist.sirs.child_locator.ws.FolloweeView;
import com.ist.sirs.child_locator.ws.InvalidLoginTime_Exception;

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
		boolean loop = true;

		clearScreen();
		System.out.println("[CREATE PIN]");
		System.out.println("Since its the first time you execute this app, choose a 4-8 digit Pin:");

		while (loop) {
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

					firstMenu();
					loop = false;
				} else
					System.out.println("Invalid pin, try again:");
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void checkPin() {
		Scanner scanner = new Scanner(System.in);
		String pin;
		int failedAttempts = 0;
		boolean loop = true;

		clearScreen();
		System.out.println("[CHECK PIN]");
		System.out.println("Insert your Pin:");

		while (loop) {
			try {
				System.out.print("Pin: ");
				pin = scanner.next();

				// hash pin and compare it
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));

				File file = new File("src/main/resources/pin");
				// init array with file length
				byte[] hashPinFile = new byte[(int) file.length()];

				FileInputStream fis = new FileInputStream(file);
				fis.read(hashPinFile); // read file into bytes[]
				fis.close();

				if (Arrays.equals(hash, hashPinFile)) {
					firstMenu();
					loop = false;
				} else {
					failedAttempts++;

					if (failedAttempts > 3) {
						try {
							// wait e^(failedAttempts) minutes
							double minutesToSleep = Math.pow(Math.E, failedAttempts);
							int millisToSleep = (int) minutesToSleep * 60 * 1000;
							System.out.format(
									"This is your %d attempt, so youll have to wait %f minutes " + "before retrying.\n",
									failedAttempts, minutesToSleep);
							Thread.sleep(millisToSleep);
						} catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
							System.out.println("Exception: " + ex.getMessage());
						}
					}

					System.out.println("Pin incorrect, try again.");

				}

			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void firstMenu() {
		Scanner scanner = new Scanner(System.in);
		String option;
		boolean loop = true;

		clearScreen();
		System.out.println("[FIRST MENU]");
		System.out.println("What do you want to do?");

		while (loop) {

			System.out.println("\t1 - Login");
			System.out.println("\t2 - Register");
			System.out.print("Number of the option: ");

			try {
				option = scanner.next();

				switch (option) {
				case "1":
					login();
					loop = false;
					break;
				case "2":
					register();
					loop = false;
					break;
				default:
					System.out.println("Invalid option, try again:");
					break;
				}
			} catch (Exception e) {
				System.out.println("[First Menu] Exception: " + e.getMessage());
			}
		}

		scanner.close();

	}

	public static void login() {
		login("");
	}

	public static void login(String exceptionMessage) {
		Scanner scanner = new Scanner(System.in);
		String phoneNumber, email, password;
		boolean loop = true;

		clearScreen();
		System.out.println("[LOGIN]");

		if (exceptionMessage != null && exceptionMessage.length() > 0)
			System.out.println(exceptionMessage);

		System.out.println("Provide your info:");

		while (loop) {

			try {
				System.out.print("Phone Number: ");
				phoneNumber = scanner.next();
				System.out.print("Email: ");
				email = scanner.next();
				System.out.print("Password: ");
				password = scanner.next();

				// TODO: check if user can login (e^c)
				if (client.login(phoneNumber, email, password)) {
					loop = false;
					mainMenu();
				} else
					System.out.println("Wrong info, try again.");

			} catch (Exception e) {
				System.out.println("[Login] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void register() {
		Scanner scanner = new Scanner(System.in);
		String phone, email, password1, password2;
		boolean loop = true;

		clearScreen();
		System.out.println("Provide your info:");

		while (loop) {
			try {
				System.out.print("Phone: ");
				phone = scanner.next();
				System.out.print("Email: ");
				email = scanner.next();
				System.out.print("Password: ");
				password1 = scanner.next();
				System.out.print("Repeat password: ");
				password2 = scanner.next();
				if (client.register(phone, email, password1, password2)) {
					loop = false;
					mainMenu();
				} else
					System.out.println("Wrong info, try again:");
			} catch (Exception e) {
				System.out.println("[Register] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void mainMenu() {
		Scanner scanner = new Scanner(System.in);
		String option;
		boolean loop = true;

		System.out.println("What do you want to do?");
		while (loop) {
			System.out.println("\t1 - Check followees");
			System.out.println("\t2 - Check followers");
			System.out.println("\t3 - Add new follower");
			System.out.println("\t4 - Print");
			System.out.print("Number of the option:");

			try {
				option = scanner.next();

				switch (option) {
				case "1":
					loop = false;
					getFollowees();
					break;
				case "2":
					loop = false;
					getFollowers();
					break;
				case "3":
					loop = false;
					addFollower();
					break;
				case "4":
					loop = false;
					client.print();
					break;
				default:
					System.out.println("Invalid login, try again:");
					break;
				}
			} catch (InvalidLoginTime_Exception e) {
				loop = false;
				login(e.getMessage());
			} catch (Exception e) {
				System.out.println("[Main Menu] Exception: " + e.getMessage());
			}
		}
		scanner.close();
	}

	public static void getFollowees() {
		List<FolloweeView> followees = client.getFollowees();
		
		clearScreen();
		System.out.println("[CHECK FOLLOWEES]");
		System.out.println("List of people you are following");
		for(int i = 0; i < followees.size(); i++){
			System.out.println("\t" + (i+1) + " - " + followees.get(i).getPhoneNumber());
		}
		
		Scanner scanner = new Scanner(System.in);
		String option;
		boolean loop = true;
		
		System.out.print("Which one you want to watch? ");
		while (loop) {
			try {
				option = scanner.next();
				Pattern pattern = Pattern.compile("^\\d$");
				Matcher matcher = pattern.matcher(option);
				
				//if its a number
				if (matcher.find()){
					int optInt = Integer.valueOf(option);
					if(optInt > 0 && optInt <= followees.size()){
						loop = false;
						checkFollowee(followees.get(optInt-1).getPhoneNumber());
					}
				}
				
			} catch (Exception e) {
				System.out.println("[Main Menu] Exception: " + e.getMessage());
			}
		}
	}

	public static void getFollowers() {

	}
	
	public static void checkFollowee(String phoneNumber){
		clearScreen();
		
		System.out.println("BAZINGA");
	}

	public static void addFollower() {

	}

	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}
