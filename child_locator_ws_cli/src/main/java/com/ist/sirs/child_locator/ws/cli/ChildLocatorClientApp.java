package com.ist.sirs.child_locator.ws.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.synth.SynthSeparatorUI;

import java.util.TimerTask;

import com.ist.sirs.child_locator.ws.FolloweeView;
import com.ist.sirs.child_locator.ws.InvalidEmail_Exception;
import com.ist.sirs.child_locator.ws.InvalidLoginTime_Exception;
import com.ist.sirs.child_locator.ws.InvalidPassword_Exception;
import com.ist.sirs.child_locator.ws.InvalidPhoneNumber_Exception;
import com.sun.xml.fastinfoset.sax.SystemIdResolver;
import com.ist.sirs.child_locator.ws.ConnectionAlreadyExists_Exception;

public class ChildLocatorClientApp {
	public static ChildLocatorClient client = null;

	public static void main(String args[]) {

		String wsURL = args[0];

		// Create client
		client = new ChildLocatorClient(wsURL);

		// System.out.println(client.print());
		// System.out.println(client.login("sheldon","bazinga"));

		// check if its the first time user executes app
		final String phone = getPhoneNumber();
		System.out.println(phone);
		java.util.Timer t = new java.util.Timer();
		//double fixedLatitude = generateCoordinate();
		
		t.schedule(new TimerTask() {
			private double fixedLatitude = generateCoordinate();
			private double fixedLongitude = generateCoordinate();
		            @Override
		            public void run() {
		        		double latitude = fixedLatitude;
		        		double longitude = fixedLongitude;
		        		fixedLatitude += 0.0000050 ;
		        		sendCoordinates(phone, latitude, longitude);}}, 0, 1000);
		
		if (!(new File("src/main/resources/pin")).isFile())
			createPin();
		else
			checkPin();
	}
	
	private static void sendCoordinates(String phone, double latitude, double longitude) {		
		//System.out.println("Latitude: " + latitude + " Longitude: " + longitude);
		try {
			client.sendCoordinates(phone, String.valueOf(latitude), String.valueOf(longitude));
		} catch (Exception e) {
			System.err.println("Failed to send coordinates: " + e.getMessage());
		}
        //System.out.println("Sending coordinates");
	}
	
	public static double generateCoordinate() {
		Random r=new Random();
		String coord=null;
		double random = Double.parseDouble(getRandomValue(r, 0, 180, 7).replace(',', '.'));
		random-=90;
		//coord=Double.toString(random);
		return random;
	}
	
	public static String getRandomValue(final Random random,
		    final int lowerBound,
		    final int upperBound,
		    final int decimalPlaces){

		    if(lowerBound < 0 || upperBound <= lowerBound || decimalPlaces < 0){
		        throw new IllegalArgumentException("Put error message here");
		    }

		    final double dbl =
		        ((random == null ? new Random() : random).nextDouble() //
		            * (upperBound - lowerBound))
		            + lowerBound;
		    return String.format("%." + decimalPlaces + "f", dbl);

		}

	private static String getPhoneNumber() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/main/resources/phoneNumber"));
			return br.readLine().trim();
		} catch (IOException e) {
			System.err.println("Your phone number is corrupt, restart the app: " + e.getMessage());
			return null;
		}
	}

	public static void createPin() {
		Scanner scanner = new Scanner(System.in);
		String pin1, pin2;
		boolean loop = true;

		clearScreen();
		System.out.println("[CREATE PIN]");
		System.out.println("Since its the first time you execute this app, choose a 4-8 digit Pin:");

		while (loop) {
			try {
				System.out.print("Pin: ");
				pin1 = scanner.next();
				System.out.print("Repeat Pin: ");
				pin2 = scanner.next();

				if (!pin1.equals(pin2)) {
					System.out.println("Pins do not match, try again:");
					continue;
				}

				// check if valid
				Pattern pattern = Pattern.compile("^\\d{4,8}$");
				Matcher matcher = pattern.matcher(pin1);
				if (matcher.find()) {
					// hash pin and save it
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					byte[] hash = digest.digest(pin1.getBytes(StandardCharsets.UTF_8));

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
					waitAttempts(++failedAttempts);
					System.out.println("Pin incorrect, try again.");
				}

			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void waitAttempts(int attempts) {
		if (attempts > 3) {
			try {
				// wait e^(failedAttempts) minutes
				double minutesToSleep = Math.pow(Math.E, attempts-3);
				int millisToSleep = (int) minutesToSleep * 60 * 10;
				System.out.format("This is your %d attempt, so youll have to wait %f minutes " + "before retrying.\n",
						attempts, minutesToSleep);
				Thread.sleep(millisToSleep);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				System.out.println("Exception: " + ex.getMessage());
			}
		}
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
		int failedAttempts = 0;

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

				String loginCode = client.login(phoneNumber, email, password);
				if(loginCode != null) {
					confirmLogin(phoneNumber, loginCode);
					mainMenu();
					loop = false;
				} else {
					waitAttempts(++failedAttempts);
					System.out.println("Login incorrect, try again.");
				}

			} catch (Exception e) {
				System.out.println("[Login] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}
	
	public static void confirmLogin(String phoneNumber, String loginCode){
		Scanner scanner = new Scanner(System.in);
		String inputCode;
		boolean loop = true;

		clearScreen();
		System.out.println("[CONFIRM LOGIN]");
		System.out.println("Confirm your login by submiting the code send via sms.");

		try {
			// open texteditor with mocked sms
			String[] cmds = { "/bin/sh", "-c", "echo '[SMS]\nLogin Code: " + loginCode + "' | open -f" };
			Process p = Runtime.getRuntime().exec(cmds);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			System.out.println("[Confirm Login] Exception: " + e.getMessage());
		} 

		while (loop) {
			try {
				System.out.print("Login Code: ");
				inputCode = scanner.next();

				if (client.confirmLogin(inputCode)) {
					mainMenu();
					loop = false;
				} else
					System.out.println("Wrong info, try again:");
			} catch (Exception e) {
				System.out.println("[ConfirmLogin] Exception: " + e.getMessage());
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

				String registerCode = client.register(phone, email, password1, password2);
				if (registerCode != null) {
					confirmRegistration(phone, registerCode);
					loop = false;
				} else
					System.out.println("Wrong info, try again:");
			} catch (Exception e) {
				System.out.println("[Register] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void confirmRegistration(String phone, String registerCode) {
		Scanner scanner = new Scanner(System.in);
		String inputCode;
		boolean loop = true;

		clearScreen();
		System.out.println("[CONFIRM REGISTRATION]");
		System.out.println("Confirm you registration by submiting the code send via sms.");

		try {
			// open texteditor with mocked sms
			String[] cmds = { "/bin/sh", "-c", "echo '[SMS]\nRegistration Code: " + registerCode + "' | open -f" };
			Process p = Runtime.getRuntime().exec(cmds);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			System.out.println("[Confirm Registration] Exception: " + e.getMessage());
		}

		while (loop) {
			try {
				System.out.print("Register Code: ");
				inputCode = scanner.next();

				if (client.confirmRegistration(inputCode)) {
					// create local file with phone number
					FileOutputStream out = new FileOutputStream("src/main/resources/phoneNumber");
					out.write(phone.getBytes(StandardCharsets.UTF_8));
					out.close();

					mainMenu();
					loop = false;
				} else
					System.out.println("Wrong info, try again:");
			} catch (Exception e) {
				System.out.println("[ConfirmRegistration] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void mainMenu(){
		mainMenu("");
	}
	
	public static void mainMenu(String message) {
		Scanner scanner = new Scanner(System.in);
		String option;
		boolean loop = true;

		clearScreen();
		System.out.println("[MAIN MENU]");
		if(message != null && message.length() > 0)
			System.out.println("\n\t[INFO] " + message+"\n");
		System.out.println("What do you want to do?");
		while (loop) {
			System.out.println("\t1 - Get followees");
			System.out.println("\t2 - Get followers");
			System.out.println("\t3 - Add new follower");
			System.out.println("\t4 - Add new followee");
			System.out.print("Number of the option:");

			try {
				option = scanner.next();

				switch (option) {
				case "1":
					getFollowees();
					loop = false;
					break;
				case "2":
					getFollowers();
					loop = false;
					break;
				case "3":
					addFollower();
					loop = false;
					break;
				case "4":
					addFollowee();
					loop = false;
					break;
				default:
					System.out.println("Invalid login, try again:");
					break;
				}
			} catch (Exception e) {
				System.out.println("[Main Menu] Exception: " + e.getMessage());
			}
		}
		scanner.close();
	}

	public static void getFollowees() {
		List<FolloweeView> followees = client.getFollowees();

		clearScreen();
		System.out.println("[GET FOLLOWEES]");
		System.out.println("You are following " + followees.size() + " people:");
		for (int i = 0; i < followees.size(); i++) {
			System.out.println("\t" + (i + 1) + " - " + followees.get(i).getPhoneNumber());
		}

		Scanner scanner = new Scanner(System.in);
		String option;
		boolean loop = true;

		System.out.print("Which one you want to watch? [q] to return to Main Menu");
		while (loop) {
			try {
				System.out.print("Option: ");
				option = scanner.next();

				// if user wants to return to main menu
				if (option.equals("q")) {
					loop = false;
					mainMenu();
				} else {
					Pattern pattern = Pattern.compile("^\\d+$");
					Matcher matcher = pattern.matcher(option);

					// if its a number
					if (matcher.find()) {
						int optInt = Integer.valueOf(option);

						// if its in a valid range
						if (optInt > 0 && optInt <= followees.size()) {
							checkFollowee(followees.get(optInt - 1).getPhoneNumber());
							loop = false;
						} else
							System.out.println("Option is not in the range, try again.");
					} else
						System.out.println("Option is not a number, try again.");
				}

			} catch (Exception e) {
				System.out.println("[Main Menu] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void getFollowers() {
		List<String> followers = client.getFollowers();

		clearScreen();
		System.out.println("[GET FOLLOWERS]");
		System.out.println("You are being followed by " + followers.size() + " people:");
		for (int i = 0; i < followers.size(); i++) {
			System.out.println("\t" + (i + 1) + " - " + followers.get(i));
		}
		
		System.out.println("\n\nPress any key to return to Main Menu.");
		try {
			System.in.read();
		} catch (IOException e) {

		} finally{
			mainMenu();
		}
	}

	public static void checkFollowee(String phoneSon) throws IOException {
		clearScreen();
		String info = client.getCoordinates(getPhoneNumber(), phoneSon);
		System.out.println(info);
		System.in.read();
	}

	public static void addFollower() {
		Scanner scanner = new Scanner(System.in);
		String phoneNumber;
		boolean loop = true;

		clearScreen();
		System.out.println("[ADD FOLLOWER]");
		System.out.println("Insert phone number of whom you want to be followed by:  [q] to return to Main Menu");
		while (loop) {

			try {
				System.out.print("Follower Phone Number: ");
				phoneNumber = scanner.next();

				if (phoneNumber.equals("q")) {
					loop = false;
					mainMenu();
				} else {
					displayNonce(phoneNumber);
					loop = false;
				}

			} catch (Exception e) {
				System.out.println("[Add Follower] Exception: " + e.getMessage());
			}
		}

		scanner.close();
	}

	public static void displayNonce(String phoneNumber) {
		clearScreen();
		System.out.println("[DISPLAY NONCE]");
		try {
			String nonce = client.getAddNonce(phoneNumber);
			System.out.println("Give this code to your follower:");
			System.out.println("\t" + nonce);

			System.out.println("\n\nPress any key to return to Main Menu.");
			System.in.read();
		} catch (Exception e) {
			System.out.print("[Display Nonce] Exception: " + e.getMessage());
		}

		mainMenu();
	}

	public static void addFollowee() {
		boolean loop = true;
		String phoneNumber, nonce;
		Scanner scanner = new Scanner(System.in);

		clearScreen();
		System.out.println("[ADD FOLLOWEE]");
		System.out.println(
				"Insert phone number of whom you want to follow "
				+ "and the code. [q] to return to Main Menu");
		while (loop) {
			try {
				System.out.print("Followee Phone Number: ");
				phoneNumber = scanner.next();
				System.out.print("Code: ");
				nonce = scanner.next();

				if (!phoneNumber.equals("q") && !nonce.equals("q"))
					client.addFollowee(phoneNumber, nonce);
				
				loop = false;
				mainMenu("Followee added successfully");
				
			} catch (Exception e) {
				System.out.println("[Add Follower] Exception: " + e.getMessage());
			}
		}
		scanner.close();
	}

	public static void removeFollowee() {

	}

	// ----------- AUX FUNCTIONS -----------

	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	public static boolean isPhoneNumber(String phoneNumber) {
		Pattern pattern = Pattern.compile("^\\d{9}$");
		Matcher matcher = pattern.matcher(phoneNumber);

		return matcher.find();
	}
}
