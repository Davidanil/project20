package com.ist.sirs.child_locator.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoUtil {
	private static Cipher cipher;

    // TODO add security helper methods
	public static byte[] asymCipher(byte[] message, Key Key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException{
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, Key);
		byte[] cipherMsg = cipher.doFinal(message);
		return cipherMsg;
	}
	
	public static byte[] asymDecipher(byte[] cipheredMsg, Key Key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException{
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, Key);
		byte[] decipherMsg = cipher.doFinal(cipheredMsg);
		return decipherMsg;
	}

}
