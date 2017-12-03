package com.ist.sirs.child_locator.ws.db;

import java.io.Reader;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public boolean login(String email, String passwordHash) {
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM login WHERE email=? AND password=?");
			stmt.setString(1, email);
			stmt.setString(2, passwordHash);
			ResultSet rs = stmt.executeQuery();
			boolean hasNext = rs.next();
			
			if(hasNext){
				PreparedStatement stmt2 = connection.prepareStatement("UPDATE login SET lastlogin=? WHERE email=?");
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				stmt2.setTimestamp(1, timestamp);
				stmt2.setString(2, email);
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

	public boolean register(String phoneNumber, String email, String salt, String hash) {
		try {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM login WHERE phone=?");
			stmt.setString(1, phoneNumber);
			ResultSet rs = stmt.executeQuery();

			// user is already registered
			if (rs.next())
				return false;

			stmt = connection
					.prepareStatement("INSERT INTO login (phone,email,salt,password,lastlogin) " + "VALUES(?,?,?,?,?)");
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());
			stmt.setString(1, phoneNumber);
			stmt.setString(2, email);
			stmt.setString(3, salt);
			stmt.setString(4, hash);
			stmt.setTimestamp(5, ts);
			int count = stmt.executeUpdate();

			// successful insert
			if (count > 0)
				return true;
			else
				return false;

		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return false;
		}
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
