package com.ist.sirs.child_locator.ws;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import com.ist.sirs.child_locator.ws.db.ChildLocatorDB;

@WebService(endpointInterface = "com.ist.sirs.child_locator.ws.ChildLocatorPortType", wsdlLocation = "childLocator.1_0.wsdl", name = "ChildLocatorService", portName = "ChildLocatorPort", targetNamespace = "http://ws.child_locator.sirs.ist.com/", serviceName = "ChildLocatorService")
// @HandlerChain(file="/child_locator_ws_handler_chain.xml")
public class ChildLocatorPortImpl implements ChildLocatorPortType {
	private static final int PASSNUMMIN = 8;
	private static final int PASSNUMMAX = 32;
	
	private ChildLocatorDB db;

	private ChildLocatorPortImpl() {
		db = ChildLocatorDB.getInstance(); 
	}

	private static class SingletonHolder {
		private static final ChildLocatorPortImpl INSTANCE = new ChildLocatorPortImpl();
	}

	public static synchronized ChildLocatorPortImpl getInstance() {
		return SingletonHolder.INSTANCE;
	}

	// -------------- SERVICE METHODS --------------

	@Override
	public String print() {
		return "bazinga";
	}

	@Override
	public boolean login(String email, String password) {
		if(isEmail(email) && isPassword(password)){
			//String passwordHash = TODO
			return db.login(email,password);
		}
		else
			return false;
	}

	// --------------- AUX METHODS -----------------

	// Checks if password has at least PASSNUM digits ["{"+ PASSNUM +",}"] and
	// does not allow whitesapces ["(?=\\S+$)"]
	private boolean isPassword(String password) {
		Pattern pattern = Pattern.compile("^(?=\\S+$).{" + PASSNUMMIN + "," + PASSNUMMAX + "}$");
		Matcher matcher = pattern.matcher(password);
		if (matcher.find())
			return true;

		System.err.println(
				"Password must be at least " + PASSNUMMIN + " characters and maximum " + PASSNUMMAX + "characters");
		return false;
	}

	// Check if it's email
	public boolean isEmail(String email) {
		Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(email);
		if (matcher.find())
			return true;

		System.err.println(email + " is not a valid email address.");
		return false;
	}

	// Checks if has 9 all integer characters
	private static boolean isPhoneNumber(String num) {
		try {
			Integer.valueOf(num);
		} catch (Exception e) {
			System.err.println("Phone number must contain only numbers");
			return false;
		}

		if (num.length() == 9 && Integer.valueOf(num) > 0)
			return true;

		System.err.println("Phone number must contain 9 digits");
		return false;

	}

}
