package com.ist.sirs.child_locator.security;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class CertUtil {

	/**
	 * Getting Public Key from certificate
	 */
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}

	public static Certificate getX509CertificateFromStream(InputStream in) throws CertificateException {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			return cert;
		} finally {
			closeStream(in);
		}
	}

	public static Certificate getX509CertificateFromString(String result) throws CertificateException {
		byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
		InputStream in = new ByteArrayInputStream(bytes);
		return getX509CertificateFromStream(in);
	}

	public static Certificate getCertificate(String certName) throws CertificateException, IOException {
		Certificate cert = null;

		// check if we already have the certificate needed
		File f = new File(certName + ".cer");
		boolean hasCert = f.isFile();
		System.out.println("HAS CERT???: " + hasCert);
		if (hasCert)
			cert = getX509CertificateFromResource(certName + ".cer");
		// // get public key from CA Certificate
		// Certificate caCert = getX509CertificateFromResource("ca.cer");
		// PublicKey caPublicKey = caCert.getPublicKey();
		//
		// // ask Mediators certificate to CA
		// String result = getCertificateResultFromCA(certName);
		// if (result == "") {
		// System.out.println("Certificate not found!");
		// return null;
		// }
		// mediatorCert = getX509CertificateFromString(result);
		// if (mediatorCert == null) {
		// System.out.println("Certificate invalid!");
		// return null;
		// }
		//
		// // confirm said certificate was signed by CA
		// boolean signed = verifySignedCertificate(mediatorCert, caPublicKey);
		// if(!signed){
		// System.out.println("Caught exception while verifying certificate with
		// CA public key");
		// return null;
		// }
		//
		// // save certificate for future use
		// String fileName = certName + ".cer";
		// BufferedWriter bw = new BufferedWriter(new
		// FileWriter("./src/main/resources/" + fileName));
		// bw.write(result);
		// bw.close();
		// }

		return cert;
	}

	public static Certificate getX509CertificateFromResource(String certificateResourcePath)
			throws IOException, CertificateException {
		InputStream is = getResourceAsStream(certificateResourcePath);
		return getX509CertificateFromStream(is);
	}

	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			return false;
		}
		return true;
	}

	/**
	 * Getting private key from keystore resource
	 */
	public static PrivateKey getPrivateKeyFromKeyStoreResource(String keyStoreResourcePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword)
			throws FileNotFoundException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore = readKeystoreFromResource(keyStoreResourcePath, keyStorePassword);
		return getPrivateKeyFromKeyStore(keyAlias, keyPassword, keystore);
	}

	public static KeyStore readKeystoreFromResource(String keyStoreResourcePath, char[] keyStorePassword)
			throws KeyStoreException {
		InputStream is = getResourceAsStream(keyStoreResourcePath);
		return readKeystoreFromStream(is, keyStorePassword);
	}

	public static PrivateKey getPrivateKeyFromKeyStore(String keyAlias, char[] keyPassword, KeyStore keystore)
			throws KeyStoreException, UnrecoverableKeyException {
		PrivateKey key;
		try {
			key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
		} catch (NoSuchAlgorithmException e) {
			throw new KeyStoreException(e);
		}
		return key;
	}
	// resource stream helpers ------------------------------------------------

	/** Method used to access resource. */
	private static InputStream getResourceAsStream(String resourcePath) {
		// uses current thread's class loader to also work correctly inside
		// application servers
		// reference: http://stackoverflow.com/a/676273/129497
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		return is;
	}

	/** Gets a KeyStore from stream **/
	private static KeyStore readKeystoreFromStream(InputStream keyStoreInputStream, char[] keyStorePassword)
			throws KeyStoreException {
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		System.out.println("STRING KEY STORE: ");
		try {
			keystore.load(keyStoreInputStream, keyStorePassword);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeyStoreException("Could not load key store", e);
		} finally {
			closeStream(keyStoreInputStream);
		}
		return keystore;
	}

	/** Do the best effort to close the stream, but ignore exceptions. */
	private static void closeStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			// ignore
		}
	}

}