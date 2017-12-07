package com.ist.sirs.child_locator.ws;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
@HandlerChain(file = "/child_locator_ws_handler_chain.xml")
public class ChildLocatorPortImpl implements ChildLocatorPortType {
	private static final int PASSNUMMIN = 8;
	private static final int PASSNUMMAX = 32;

	@Resource
	private WebServiceContext webServiceContext;

	private ChildLocatorDB db;
	
	private HashMap<String, SecretKey> keys = new HashMap<String, SecretKey>();
	
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
	public String print() throws InvalidLoginTime_Exception {
		if (!validLoginTime())
			throwInvalidLoginTimeException("Your last login was over a day ago, you need to re-login.");
		return "bazinga";
	}
	
	@Override
	public String createChannel(String phoneNumber, String publicKey){
		SecretKey secretKey = null;
		byte[] keyBytes = null;
		PublicKey publickey = null;
		Cipher cipher = null;
		if(keys.get(phoneNumber) != null) //number already exists
			return null;
		try {
			// decode public key from string
			publickey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
			// generate Symmetric key
			secretKey = KeyGenerator.getInstance("AES").generateKey();
			//encrypt Symmetric key
			cipher = Cipher.getInstance("RSA");
	        cipher.init(Cipher.PUBLIC_KEY, publickey);
	        keyBytes = cipher.doFinal(secretKey.getEncoded());
		} catch (Exception e) { System.out.println(e);}
		keys.put(phoneNumber, secretKey); // add key to hash map
		return Base64.getEncoder().encodeToString(keyBytes);
	}

	@Override
	public String login(String phoneNumber, String email, String password)
			throws InvalidPhoneNumber_Exception, InvalidEmail_Exception, InvalidPassword_Exception {
		if (isPhoneNumber(phoneNumber) && isEmail(email) && isPassword(password)) {
			// salt and hash password
			String salt = db.getSalt(phoneNumber);
			byte[] hashedPassword = hashPassword(password, ToByteArray(salt));
			
			String loginCode = generateNonce();
			if(db.login(phoneNumber, email, toHexString(hashedPassword), loginCode))
				return loginCode;
		}

		return null;
	}
	
	@Override
	public boolean confirmLogin(String code){
		String phoneNumber = getPhoneNumberFromHandler();
		
		return db.confirmLogin(phoneNumber, code);
	}

	@Override
	public String register(String phoneNumber, String email, String password1, String password2)
			throws InvalidPhoneNumber_Exception, InvalidEmail_Exception, InvalidPassword_Exception,
			DifferentPasswords_Exception {
		if (isPhoneNumber(phoneNumber) && isEmail(email) && isPassword(password1) && isPassword(password2)) {
			if (!password1.equals(password2))
				throwDifferentPasswords();

			byte[] salt = generateSalt();
			byte[] hash = hashPassword(password1, salt);
			String registerCode = generateNonce();
			if(db.register(phoneNumber, email, toHexString(salt), toHexString(hash), registerCode)){
				return registerCode;
			}
		}
		return null;
	}
	
	@Override
	public boolean confirmRegistration(String code){
		String phoneNumber = getPhoneNumberFromHandler();
		
		return db.confirmRegistration(phoneNumber, code);
	}

	@Override
	public List<FolloweeView> getFollowees() {
		String phoneNumber = getPhoneNumberFromHandler();

		List<String> followees = db.getFollowees(phoneNumber);
		List<FolloweeView> followeesList = new ArrayList<FolloweeView>();
		for (String followee : followees) {
			FolloweeView fv = new FolloweeView();
			fv.setPhoneNumber(followee);
			followeesList.add(fv);
		}

		return followeesList;
	}
	
	@Override
	public List<String> getFollowers(){
		String phoneNumber = getPhoneNumberFromHandler();

		List<String> followers = db.getFollowers(phoneNumber);
	
		return followers;
	}

	@Override
	public String getAddNonce(String followerPhoneNumber)
			throws ConnectionAlreadyExists_Exception, InvalidPhoneNumber_Exception {
		// get phonenumber from handler
		String phoneNumber = getPhoneNumberFromHandler();

		// validates phonenumber, throws exception if it isnt
		isPhoneNumber(followerPhoneNumber);

		// generate nonce
		String nonce = generateNonce();
		
		// check if user is already added
		if (db.isAlreadyFollowedBy(phoneNumber, followerPhoneNumber))
			throwConnectionAlreadyExistsException(followerPhoneNumber + " is already following you");

		// if is already being followed but was never connected and nonce is
		// still valid, return same nonce
		// otherwise return new one and update db
		String nonceDB = db.isFollowedButNotConnected(phoneNumber, followerPhoneNumber);
		if (nonceDB != null)
			return nonceDB;

		// insert nonce into connected table
		// TODO: throw exception
		db.insertNonce(phoneNumber, followerPhoneNumber, nonce);
		return nonce;
	}

	@Override
	public boolean addFollowee(String phoneNumber, String nonce)
			throws InvalidPhoneNumber_Exception, InvalidNonce_Exception {
		if (isPhoneNumber(phoneNumber) && isNonce(nonce)) {
			String followerPhoneNumber = getPhoneNumberFromHandler();
			return db.addFollowee(phoneNumber, followerPhoneNumber, nonce);
		}
		
		return false;
	}
	
	@Override
	public boolean sendCoordinates(String phone, String latitude, String longitude) {
		return db.sendCoodinates(phone, latitude, longitude);
	}
	
	public String getCoordinates(String phoneDad, String phoneSon) {
		return db.getCoodinates(phoneDad, phoneSon);
	}

	// --------------- AUX METHODS -----------------
	// true - no need to re-login
	// false - force client to re-login
	public boolean validLoginTime() {
		MessageContext messageContext = webServiceContext.getMessageContext();

		Timestamp time = (Timestamp) messageContext.get(TimeHandler.CONTEXT_PROPERTY);
		String phoneNumber = getPhoneNumberFromHandler();

		try {
			if (isPhoneNumber(phoneNumber)) {
				Timestamp lastLogin = db.getLoginTime(phoneNumber);

				Calendar cal = Calendar.getInstance();
				cal.setTime(lastLogin);
				cal.add(Calendar.DAY_OF_WEEK, 1);
				lastLogin = new Timestamp(cal.getTime().getTime());

				boolean validLoginTime = time.before(lastLogin);

				return validLoginTime;
			}
		} catch (InvalidPhoneNumber_Exception e) {
			return false;
		}

		return false;
	}

	// Checks if has 9 all integer characters
	private boolean isPhoneNumber(String num) throws InvalidPhoneNumber_Exception {
		Pattern pattern = Pattern.compile("^\\d{9}$");
		Matcher matcher = pattern.matcher(num);
		if (matcher.find())
			return true;

		throwInvalidPhoneNumber();

		return false;
	}

	// Check if it's email
	private boolean isEmail(String email) throws InvalidEmail_Exception {
		Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(email);
		if (matcher.find())
			return true;

		throwInvalidEmail();

		return false;
	}
	
	private boolean isNonce(String nonce) throws InvalidNonce_Exception{
		Pattern pattern = Pattern.compile("^[A-Z0-9-_]{12}$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(nonce);
		if (matcher.find())
			return true;
		
		throwInvalidNonce("Invalid Nonce.");

		return false;
	}

	// Checks if password has at least PASSNUM digits ["{"+ PASSNUM +",}"] and
	// does not allow white spaces ["(?=\\S+$)"]
	private boolean isPassword(String password) throws InvalidPassword_Exception {
		Pattern pattern = Pattern.compile("^(?=\\S+$).{" + PASSNUMMIN + "," + PASSNUMMAX + "}$");
		Matcher matcher = pattern.matcher(password);
		if (matcher.find())
			return true;

		throwInvalidPassword();

		return false;
	}

	public byte[] hashPassword(String password, byte[] salt) {
		char[] pass = password.toCharArray();
		PBEKeySpec spec = new PBEKeySpec(pass, salt, 20000, 32 * 8);
		SecretKeyFactory skf;
		byte[] hash = null;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			hash = skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.err.println("Failed to generate hash for password " + e);
		}

		// System.out.println(toHexString(hash));
		return hash;
	}

	public byte[] generateSalt() {
		SecureRandom random = null;
		try {
			new SecureRandom();
			// The name of the pseudo-random number generation (PRNG) algorithm
			// supplied by the SUN provider
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Failed to generate salt " + e);
		}
		byte bytes[] = new byte[32];
		// System.out.println( random.getAlgorithm());
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
		if (s.isEmpty())
			return data;
		int len = s.length();
		data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public String getPhoneNumberFromHandler() {
		MessageContext messageContext = webServiceContext.getMessageContext();

		String id1 = (String) messageContext.get(IdHandler.CONTEXT_PROPERTY);
		String id2 = (String) messageContext.get(LoginRegisterIdHandler.CONTEXT_PROPERTY);

		return id1 != null && id1.length() > 0 ? id1 : id2;
	}

	private String generateNonce(){
		// https://tools.ietf.org/rfc/rfc4648.txt [page 8]
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[6];
		random.nextBytes(bytes);
		return toHexString(bytes).toUpperCase();
	}

	/** Helper method to throw new InvalidTimeException exception */
	private void throwInvalidPhoneNumber() throws InvalidPhoneNumber_Exception {
		String message = "Invalid phone number. It must have 9 digits.";
		InvalidPhoneNumber faultInfo = new InvalidPhoneNumber();
		faultInfo.setMessage(message);
		throw new InvalidPhoneNumber_Exception(message, faultInfo);
	}

	private void throwInvalidEmail() throws InvalidEmail_Exception {
		String message = "Invalid email. Example: sheldon@bazinga.com";
		InvalidEmail faultInfo = new InvalidEmail();
		faultInfo.setMessage(message);
		throw new InvalidEmail_Exception(message, faultInfo);
	}

	private void throwInvalidPassword() throws InvalidPassword_Exception {
		String message = "Password must be at least " + PASSNUMMIN + " characters and maximum " + PASSNUMMAX
				+ "characters";
		InvalidPassword faultInfo = new InvalidPassword();
		faultInfo.setMessage(message);
		throw new InvalidPassword_Exception(message, faultInfo);
	}
	
	private void throwInvalidNonce(String message) throws InvalidNonce_Exception {
		InvalidNonce faultInfo = new InvalidNonce();
		faultInfo.setMessage(message);
		throw new InvalidNonce_Exception(message, faultInfo);
	}

	private void throwDifferentPasswords() throws DifferentPasswords_Exception {
		String message = "Passwords dont match.";
		DifferentPasswords faultInfo = new DifferentPasswords();
		faultInfo.setMessage(message);
		throw new DifferentPasswords_Exception(message, faultInfo);
	}

	private void throwInvalidLoginTimeException(final String message) throws InvalidLoginTime_Exception {
		InvalidLoginTime faultInfo = new InvalidLoginTime();
		faultInfo.setMessage(message);
		throw new InvalidLoginTime_Exception(message, faultInfo);
	}

	private void throwConnectionAlreadyExistsException(final String message) throws ConnectionAlreadyExists_Exception {
		ConnectionAlreadyExists faultInfo = new ConnectionAlreadyExists();
		faultInfo.setMessage(message);
		throw new ConnectionAlreadyExists_Exception(message, faultInfo);
	}
	

}
