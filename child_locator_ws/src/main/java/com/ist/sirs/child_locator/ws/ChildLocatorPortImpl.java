package com.ist.sirs.child_locator.ws;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import com.ist.sirs.child_locator.handlers.IdHandler;
import com.ist.sirs.child_locator.handlers.LoginRegisterIdHandler;
import com.ist.sirs.child_locator.handlers.TimeHandler;
import com.ist.sirs.child_locator.ws.db.ChildLocatorDB;

@WebService(endpointInterface = "com.ist.sirs.child_locator.ws.ChildLocatorPortType", wsdlLocation = "childLocator.1_0.wsdl", name = "ChildLocatorService", portName = "ChildLocatorPort", targetNamespace = "http://ws.child_locator.sirs.ist.com/", serviceName = "ChildLocatorService")
@HandlerChain(file="/child_locator_ws_handler_chain.xml")
public class ChildLocatorPortImpl implements ChildLocatorPortType {
	private static final int PASSNUMMIN = 8;
	private static final int PASSNUMMAX = 32;
	
	@Resource
	private WebServiceContext webServiceContext;
	
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
	public String print() throws InvalidLoginTime_Exception{
		if(!validLoginTime())
			throwInvalidLoginTimeException("Your last login was over a day ago, you need to re-login.");
		return "bazinga";
	}

	@Override
	public boolean login(String phoneNumber, String email, String password) {
		if(isPhoneNumber(phoneNumber) && isEmail(email) && isPassword(password)){
			//salt and hash password
			String salt = db.getSalt(phoneNumber);
			byte[] hashedPassword = hashPassword(password, ToByteArray(salt));
			return db.login(email,toHexString(hashedPassword));
		}
		
		return false;
	}
	
	@Override
	public boolean register(String phoneNumber, String email, String password1, String password2){
		if(isPhoneNumber(phoneNumber) && isEmail(email) && isPassword(password1)
				&& isPassword(password2) && password1.equals(password2)){
			byte[] salt = generateSalt();
			byte[] hash = hashPassword(password1, salt);
			return db.register(phoneNumber, email, toHexString(salt), toHexString(hash));
		}
		
		return false;	
	}
	
	//true - no need to re-login
	//false - force client to re-login
	public boolean validLoginTime(){
		MessageContext messageContext = webServiceContext.getMessageContext();

		Timestamp time = (Timestamp) messageContext.get(TimeHandler.CONTEXT_PROPERTY);
		String id1 = (String) messageContext.get(IdHandler.CONTEXT_PROPERTY);
		String id2 = (String) messageContext.get(LoginRegisterIdHandler.CONTEXT_PROPERTY);
		
		String phoneNumber = id1.length() > 0 ? id1 : id2;
		
		if(isPhoneNumber(phoneNumber)){
			Timestamp lastLogin = db.getLoginTime(phoneNumber);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(lastLogin);
			cal.add(Calendar.DAY_OF_WEEK, 1);
			lastLogin = new Timestamp(cal.getTime().getTime());
			
			boolean validLoginTime = time.before(lastLogin);
			
			return validLoginTime;
		}
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
	
	public byte[] hashPassword(String password, byte[] salt) {
		char[] pass = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(pass, salt, 20000, 32 * 8);
        SecretKeyFactory skf;
        byte[] hash=null;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			hash = skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.err.println("Failed to generate hash for password " + e);
		}
		
        //System.out.println(toHexString(hash));
		return hash;
	}
	
	public byte[] generateSalt() {
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
	
	public String toHexString(byte[] bytes) {
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
	
	public byte[] ToByteArray(String s) {
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
	
	/** Helper method to throw new InvalidTimeException exception */
	private void throwInvalidLoginTimeException(final String message) throws InvalidLoginTime_Exception{
		InvalidLoginTime faultInfo = new InvalidLoginTime();
		faultInfo.setMessage(message);
		throw new InvalidLoginTime_Exception(message, faultInfo);
	}

}
