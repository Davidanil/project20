import java.io.Reader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


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
	
	//FIXME Register is almost complete, need to generate salt and hash using java.Secure.Random
	public static void main(String[] args) throws SQLException, NoSuchAlgorithmException {
		
/*		Connection conn = connectDB();
	
 		PRINT ALL TABLES
		ResultSet rs = doQuery(conn, "SELECT * FROM login");
		printLoginTable(rs);
		System.out.println();
		rs = doQuery(conn, "SELECT * FROM connected");
		printConnectedTable(rs);
		conn.close();
*/		
		
		//REGISTER
		Scanner in = new Scanner(System.in);
		
		System.out.println("Input phone number: ");
		String phone = in.nextLine();
//		isPhoneNumber(phone);

		System.out.println("Input email: ");
		String email = in.nextLine();
//		System.out.println(isEmail(email));
		
		System.out.println("Input password: ");
		String password = in.nextLine();
//		isPassword(password);
		
		System.out.println("Confirm password: ");
		String password2 = in.nextLine();
//		isPasswordEqual(password, password2);
		
		if(isPhoneNumber(phone) && isEmail(email) && isPassword(password) && isPasswordEqual(password, password2))
				System.out.println("Submiting data...");
		else main(args);
		
		//Generate SALT to be on server side TODO
		//HASH PASSWORD to be on server side TODO
		
		//String to be on server side to insert into database TODO
		//String s = "INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`attempts`,`verified`) VALUES "
		//+ "(" + phone + "," + email + "," + salt + "," + pass + ", 0, 0)";
		
	}
	
}
