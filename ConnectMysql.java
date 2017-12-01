import java.io.Reader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;


public class ConnectMysql {
	
	private static final int PASSNUM = 4;
	private static final String USER = "root";
	private static final String PASS = "toor";

	public static Connection connectDB() {
		Connection c = null;
		try {
			c = DriverManager.getConnection("jdbc:mysql://localhost/childdb", USER, PASS);
			System.out.println("Success! Conected to db!");
		} catch (Exception e) {
			System.err.println("Failed to connect to database: " + e );
		}
		return c;
	}
	
	
	//Execute Query
	public static ResultSet doQuery(Connection conn, String query) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn.setSchema("childdb");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			System.err.println("Failed to query: " + e);
		}
		
		return rs;
	}
	
	//Print login table
	public static void printLoginTable(ResultSet rs) throws SQLException {
		System.out.println("-----Login Table-----");
		System.out.println(" phone| email| salt| pass| attempts| verified");
		while (rs.next()){
	        String phone = rs.getString("phone");
	        String email = rs.getString("email");
	        String salt = rs.getString("salt");
	        String pass = rs.getString("password");
	        int attempts = rs.getInt("attempts");
	        int verified = rs.getInt("verified");
	        // print the results
	        System.out.format("%s| %s| %s| %s| %s| %s\n", phone, email, salt, pass, attempts, verified);
	        }
		System.out.println("-------------------------");
	}
	
	//Print connected table
	public static void printConnectedTable(ResultSet rs) throws SQLException {
		System.out.println("-----Connected Table-----");
        System.out.println(" connectid| phone| phone2| connected| nonce| timestamp");
		while (rs.next()){
	        int connectid = rs.getInt("connectid");
	        String phone = rs.getString("phone");
	        String phone2 = rs.getString("phone2");
	        int connected = rs.getInt("connected");
	        int nonce = rs.getInt("nonce");
	        Date timestamp = rs.getDate("timestamp");
	        // print the results
	        System.out.format(" %s| %s| %s| %s| %s| %s\n", connectid, phone, phone2, connected, nonce, timestamp);
	        }
		System.out.println("-------------------------");
	}

	//Check if passwords are the same
	private static boolean isPasswordEqual(String password, String password2) {
		if(isPassword(password2) && password.equals(password2)) return true;
		
		System.err.println("Passwords do not match");
		return false;
		
		
	}

//Checks if password has at least PASSNUM digits ["{"+ PASSNUM +",}"] and does not allow whitesapces ["(?=\\S+$)"]
	private static boolean isPassword(String password) {
		Pattern pattern = Pattern.compile("^(?=\\S+$).{"+ PASSNUM +",}$");
		Matcher matcher = pattern.matcher(password);
		if(matcher.find()) return true;
		
		System.err.println("Password must be at least " + PASSNUM +" characters long");
		return false;		
	}

//Check if it's email	
	public static boolean isEmail(String email) {
			Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(email);
			if(matcher.find()) return true;
			
			System.err.println(email + " is not a valid email address.");
			return false;
		}

	//Checks if has 9 all integer characters
	private static boolean isPhoneNumber(String num) {
		try {
			Integer.valueOf(num);
		} catch (Exception e) {
			System.err.println("Phone number must contain only numbers");
			return false;
		}
		
		if(num.length()==9 && Integer.valueOf(num)>0) return true;
		
		System.err.println("Phone number must contain 9 digits");
		return false;
		
	}
	
	
	
	private static byte[] generateSalt() {
		SecureRandom random = null;
		try {
			new SecureRandom();
			//The name of the pseudo-random number generation (PRNG) algorithm supplied by the SUN provider
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Failed to generate salt " + e);
		}
        byte bytes[] = new byte[32];
        //System.out.println( random.getAlgorithm());
        random.nextBytes(bytes);        
        return bytes;

	}
	
	public static String hashPassword(char[] pass, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(pass, salt, 20000, 32 * 8);
        SecretKeyFactory skf;
        byte[] hash=null;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			hash = skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.err.println("Failed to generate hash for password " + e);
		}
		
        return toHexString(hash);
	}
	
		public static String toHexString(byte[] bytes) {
		    StringBuilder hexString = new StringBuilder();
	
		    for (int i = 0; i < bytes.length; i++) {
		        String hex = Integer.toHexString(0xFF & bytes[i]);
		        if (hex.length() == 1) {
		            hexString.append('0');
		        }
		        hexString.append(hex);
		    }
	
		    return hexString.toString();
		}

		
		public static byte[] ToByteArray(String s) {
			byte[] data = null;
			if(s.isEmpty()) return data;
		    int len = s.length();
		    data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2) {
		        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
		    return data;
		}
	
	public static void main(String[] args) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		
		Connection conn = connectDB();
		ResultSet rs;
		
/*		Insert into database with hash salt
 
		String delete = "DELETE FROM `childdb`.`login` WHERE `phone`='913435145'";
		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(delete);
		
		String phone="913435145";
		String email="DAVIDD@hotmail.com";
		String salt = toHexString(generateSalt());
		String password= "teste";
		String pass = hashPassword(password.toCharArray(), ToByteArray(salt));
		
		//System.out.println(pass);
        
        String insert = "INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`attempts`,`verified`, `lastlogin`) VALUES "
        		+ "('" + phone + "','" + email + "','" + salt + "','" + pass + "', 0, 0, now())";
        //System.out.println(insert);
        
        stmt = conn.createStatement();
		stmt.executeUpdate(insert);
        
        //checks if its right
        rs = doQuery(conn, "Select * from login");
        printLoginTable(rs);*/
/*****************************************************************************************************/
		Scanner in = new Scanner(System.in);
    	System.out.println("Input phone number: ");
		String phone = in.nextLine();
    	System.out.println("Input pass: ");
		String pass = in.nextLine();
		
		//USE INSERT ABOVE
		login(phone, pass);
		
	}
	
	private static boolean login(String userphone, String userpass) throws SQLException {
		
		//Login
    	
		Connection conn = connectDB();
		ResultSet rs;
		
		String queryGetSaltAndPass = "SELECT salt, password from login where phone='"+ userphone + "'";
		//System.out.println(queryGetSaltAndPass);
		String saltFromDB = null;
		String passHashedFromDB = null;
		try {
			rs=doQuery(conn, queryGetSaltAndPass);
			while (rs.next()){
				saltFromDB = rs.getString("salt");
				passHashedFromDB = rs.getString("password");
			}
		} catch (SQLException e) {
			System.err.println("Failed to retrieve salt and pass " + e);
			return false;
		}
		
		//could not retrieve db's salt, ergo login failed
		if(saltFromDB==null) {
			System.err.println("Login failed");
			return false;
		}
		
		//Hash user's input with salt from db
		String userPassHashed = hashPassword(userpass.toCharArray(), ToByteArray(saltFromDB));
		
		//Result compare
		System.out.println(userPassHashed.equals(passHashedFromDB));
    	conn.close();
		return true;
	}
	
	
}
