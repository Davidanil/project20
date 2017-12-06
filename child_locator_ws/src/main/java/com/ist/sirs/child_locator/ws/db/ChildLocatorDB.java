package com.ist.sirs.child_locator.ws.db;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ist.sirs.child_locator.handlers.TimeHandler;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ChildLocatorDB {

	private static final String USER = "root";
	private static final String PASS = "toor";

	private Connection connection = null;

	private static class SingletonHolder {
		private static final ChildLocatorDB INSTANCE = new ChildLocatorDB();
	}

	public static synchronized ChildLocatorDB getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private ChildLocatorDB() {
		try {
			this.connection = DriverManager.getConnection("jdbc:mysql://localhost/childdb", USER, PASS);
			this.connection.setSchema("childdb");
		} catch (SQLException e) {
			this.connection = null;
		}
	}

	// Execute Query
	public ResultSet doQuery(String query) throws SQLException {
		Statement stmt = this.connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		return rs;
	}

 
	//DAVID AQUI! FIXME PUT ME ON WSDL DUDE!
	public boolean sendCoodinates(String phone, String latitude, String longitude) {
		
		try{
			PreparedStatement stmt = connection
				.prepareStatement("INSERT INTO position (phone,latitude,longitude,timestamp) VALUES(?,?,?,now())");
		stmt.setString(1, phone);
		stmt.setString(2, latitude);
		stmt.setString(3, longitude);
		return stmt.executeUpdate() == 1;
		} catch (SQLException e) {
			System.err.print(e.getMessage());
			return false;
		}
	}

	public String getCoodinates(String phoneDad, String phoneSon) {
		if (!isConnected(phoneDad, phoneSon))
			return null;

		String latitude = null;
		String longitude = null;
		Timestamp timestamp = null;
		ResultSet rs = null;

		try {
			PreparedStatement stmt2 = connection
					.prepareStatement("SELECT latitude, longitude, timestamp FROM position WHERE phone=?");
			stmt2.setString(1, phoneSon);
			stmt2.executeUpdate();

			while (rs.next()) {
				latitude = rs.getString("latitude");
				longitude = rs.getString("latitude");
				timestamp = rs.getTimestamp("timestamp");
			}
			String info = latitude + ";" + longitude + ";" + timestamp;
			return info;

		} catch (SQLException e) {
			System.err.print(e.getMessage());
			return null;
		}
	}

	private boolean isVerified(String phone) {
		ResultSet rs=null;
		try {
		PreparedStatement stmt = connection.prepareStatement("SELECT * FROM login WHERE phone=?");
		stmt.setString(1, phone);
		rs = stmt.executeQuery();
		
		if (!rs.next()) return false;
		if (!rs.getString("verified").equals("1")) return false;
		}
		catch (SQLException e) {
			System.err.print(e.getMessage());
			return false;
		}
		
		return true;
		
	}

	private boolean isConnected(String phoneSon, String phoneDad) {
		PreparedStatement stmt;
		String connected = null;
		try {
			stmt = connection.prepareStatement("SELECT connected FROM connected WHERE phone=? AND phone2=?");
			stmt.setString(1, phoneSon);
			stmt.setString(2, phoneDad);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				connected = rs.getString("connected");
			}

		} catch (SQLException e) {
			System.err.print(e.getMessage());
			return false;
		}
		if (!connected.equals("1"))
			return false;

		return true;

	}

	public boolean login(String phoneNumber, String email, String passwordHash) {
		if(!isVerified(phoneNumber)) return false;
		try {
			// check if phonenumber exists in db
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM login WHERE phone=?");
			stmt.setString(1, phoneNumber);
			ResultSet rs = stmt.executeQuery();
			boolean hasNext = rs.next();

			if (hasNext) {
				//
				PreparedStatement stmt0 = connection
						.prepareStatement("SELECT * FROM login WHERE phone=? AND email=? AND password=?");
				stmt0.setString(1, phoneNumber);
				stmt0.setString(2, email);
				stmt0.setString(3, passwordHash);
				ResultSet rs0 = stmt0.executeQuery();
				hasNext = rs0.next();

				if (hasNext) {
					PreparedStatement stmt1 = connection
							.prepareStatement("UPDATE login SET attempts=0, lastlogin=? WHERE phone=?");
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					stmt1.setTimestamp(1, timestamp);
					stmt1.setString(2, phoneNumber);
					stmt1.executeUpdate();
				}
			} else {
				PreparedStatement stmt2 = connection
						.prepareStatement("UPDATE login SET attempts=attemps+1 WHERE phone=?");
				stmt2.setString(1, phoneNumber);
				stmt2.executeUpdate();
			}

			return hasNext;

		} catch (SQLException e) {
			System.err.print(e.getMessage());
			return false;
		}
	}

	public String getSalt(String phoneNumber) {
		String salt = "";
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT salt FROM login WHERE phone=?");
			stmt.setString(1, phoneNumber);
			ResultSet rs = stmt.executeQuery();

			if (rs.next())
				salt = rs.getString("salt");

		} catch (SQLException e) {
			System.out.println("[DB - getSalt] Exception - " + e.getMessage());
		}
		return salt;
	}

	public boolean register(String phoneNumber, String email, String salt, String hash, String registerCode) {
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM login WHERE phone=?");
			stmt.setString(1, phoneNumber);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// user is already registered and verified
				if (rs.getInt("verified") == 1)
					return false;

				// user registered but never verified: update nounce and
				// lastlogin and return nonce
				PreparedStatement stmt1 = connection
						.prepareStatement("UPDATE login SET lastlogin=?, registercode=? WHERE phone=?");
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				stmt1.setTimestamp(1, timestamp);
				stmt1.setString(2, registerCode);
				stmt1.setString(3, phoneNumber);

				return stmt1.executeUpdate() == 1;
			}

			PreparedStatement stmt2 = connection.prepareStatement(
					"INSERT INTO login (phone,email,salt,password,lastlogin,registercode) " + "VALUES(?,?,?,?,?,?)");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			stmt2.setString(1, phoneNumber);
			stmt2.setString(2, email);
			stmt2.setString(3, salt);
			stmt2.setString(4, hash);
			stmt2.setTimestamp(5, timestamp);
			stmt2.setString(6, registerCode);
			return stmt2.executeUpdate() == 1;

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		return false;
	}

	public boolean confirmRegistration(String phoneNumber, String code) {
		try {
			PreparedStatement stmt = connection
					.prepareStatement("SELECT phone FROM login WHERE phone=? AND registercode=?");
			stmt.setString(1, phoneNumber);
			stmt.setString(2, code);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// update verified to 1
				PreparedStatement stmt1 = connection.prepareStatement("UPDATE login SET verified=1 WHERE phone=?");
				stmt1.setString(1, phoneNumber);

				return stmt1.executeUpdate() == 1;
			}

		} catch (SQLException e) {
			System.out.println("[DB - confirmRegistration] Exception - " + e.getMessage());
		}

		return false;
	}

	public Timestamp getLoginTime(String phoneNumber) {
		Timestamp time = null;
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT lastlogin FROM login WHERE phone=?");
			stmt.setString(1, phoneNumber);
			ResultSet rs = stmt.executeQuery();

			if (rs.next())
				time = rs.getTimestamp("lastlogin");

		} catch (SQLException e) {
			System.out.println("[DB - getLoginTime] Exception - " + e.getMessage());
		}
		return time;
	}

	public List<String> getFollowees(String phoneNumber) {
		List<String> phoneNumbers = new ArrayList<String>();
		try {
			PreparedStatement stmt = connection
					.prepareStatement("SELECT followeePhone FROM connected WHERE followerPhone=?");
			stmt.setString(1, phoneNumber);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				phoneNumbers.add(rs.getString("followeePhone"));
			}

		} catch (SQLException e) {
			System.out.println("[DB - getFollowees] Exception - " + e.getMessage());
		}

		return phoneNumbers;
	}

	// connection in db and already confirmed
	public boolean isAlreadyFollowedBy(String followeePhoneNumber, String followerPhoneNumber) {
		try {
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT connectid FROM connected WHERE followeePhone=? AND followerPhone=? AND connected=1");
			stmt.setString(1, followeePhoneNumber);
			stmt.setString(2, followerPhoneNumber);
			ResultSet rs = stmt.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			System.out.println("[DB - isAlreadyFollowedBy] Exception - " + e.getMessage());
			return false;
		}
	}

	// connection is already in db but was never confirmed. get nonce if its
	// still valid, null otherwise
	public String isFollowedButNotConnected(String followeePhoneNumber, String followerPhoneNumber) {
		try {
			int seconds = 0;
			Properties prop = new Properties();
			prop.load(ChildLocatorDB.class.getResourceAsStream("/config.properties"));
			seconds = Integer.parseInt(prop.getProperty("nonceTimeout"));

			PreparedStatement stmt = connection.prepareStatement("SELECT nonce FROM connected "
					+ "WHERE followeePhone=? AND followerPhone=? AND connected=0 AND UNIX_TIMESTAMP(NOW())-UNIX_TIMESTAMP(timestamp) < ?");
			stmt.setString(1, followeePhoneNumber);
			stmt.setString(2, followerPhoneNumber);
			stmt.setInt(3, seconds);
			ResultSet rs = stmt.executeQuery();

			if (rs.next())
				return rs.getString(0);
			return null;

		} catch (IOException | SQLException e) {
			System.out.println("[DB - isFollowedButNotConnected] Exception - " + e.getMessage());
			return null;
		}
	}

	//
	public boolean addFollowee(String followeePhoneNumber, String followerPhoneNumber, String nonce) {
		try {
			// check if connection already exists
			PreparedStatement stmt0 = connection.prepareStatement(
					"SELECT connectid FROM connected WHERE followeePhone=? AND followerPhone=? AND nonce=?");
			stmt0.setString(1, followeePhoneNumber);
			stmt0.setString(2, followerPhoneNumber);
			stmt0.setString(3, nonce);
			ResultSet rs = stmt0.executeQuery();
			boolean hasNext = rs.next();

			// update connected to 1
			if (hasNext) {
				PreparedStatement stmt1 = connection.prepareStatement(
						"UPDATE connected SET connected=1 WHERE followeePhone=? AND followerPhone=? AND nonce=?");
				stmt1.setString(1, followeePhoneNumber);
				stmt1.setString(2, followerPhoneNumber);
				stmt1.setString(3, nonce);
				int count = stmt1.executeUpdate();

				return count > 0;
			}

			return hasNext;

		} catch (SQLException e) {
			System.err.print(e.getMessage());
			return false;
		}
	}

	// insert nonce to db. if conection already exists, update
	// return true if changes were made
	public boolean insertNonce(String followeePhoneNumber, String followerPhoneNumber, String nonce) {
		try {
			// check if connection already exists
			PreparedStatement stmt0 = connection
					.prepareStatement("SELECT connectid FROM connected WHERE followeePhone=? AND followerPhone=?");
			stmt0.setString(1, followeePhoneNumber);
			stmt0.setString(2, followerPhoneNumber);
			ResultSet rs = stmt0.executeQuery();

			// update
			if (rs.next()) {
				PreparedStatement stmt1 = connection.prepareStatement(
						"UPDATE connected SET nonce=?, timestamp=NOW() WHERE followeePhone=? AND followerPhone=?");
				stmt1.setString(1, nonce);
				stmt1.setString(2, followeePhoneNumber);
				stmt1.setString(3, followerPhoneNumber);
				int count1 = stmt1.executeUpdate();

				return count1 > 0;
			}

			// insert
			PreparedStatement stmt2 = connection
					.prepareStatement("INSERT INTO connected(followeePhone, followerPhone, connected, nonce, timestamp)"
							+ "VALUES(?,?,0,?,NOW())");
			stmt2.setString(1, followeePhoneNumber);
			stmt2.setString(2, followerPhoneNumber);
			stmt2.setString(3, nonce);
			int count2 = stmt2.executeUpdate();

			return count2 > 0;

		} catch (SQLException e) {
			System.out.println("[DB - insertNonce] Exception - " + e.getMessage());
			return false;
		}
	}

	// Print login table
	public static void printLoginTable(ResultSet rs) throws SQLException {
		System.out.println("-----Login Table-----");
		System.out.println(" phone| email| salt| pass| attempts| verified");
		while (rs.next()) {
			String phone = rs.getString("phone");
			String email = rs.getString("email");
			String salt = rs.getString("salt");
			String pass = rs.getString("password");
			int attempts = rs.getInt("attempts");
			int verified = rs.getInt("verified");
			Timestamp lastlogin = rs.getTimestamp("lastlogin");
			// print the results
			System.out.format("%s| %s| %s| %s| %s| %s| %s\n", phone, email, salt, pass, attempts, verified, lastlogin);
		}
		System.out.println("-------------------------");
	}

	// Print connected table
	public static void printConnectedTable(ResultSet rs) throws SQLException {
		System.out.println("-----Connected Table-----");
		System.out.println(" connectid| phone| phone2| connected| nonce| timestamp");
		while (rs.next()) {
			int connectid = rs.getInt("connectid");
			String phone = rs.getString("phone");
			String phone2 = rs.getString("phone2");
			int connected = rs.getInt("connected");
			int nonce = rs.getInt("nonce");
			Timestamp timestamp = rs.getTimestamp("timestamp");
			// print the results
			System.out.format(" %s| %s| %s| %s| %s| %s\n", connectid, phone, phone2, connected, nonce, timestamp);
		}
		System.out.println("-------------------------");
	}

}
