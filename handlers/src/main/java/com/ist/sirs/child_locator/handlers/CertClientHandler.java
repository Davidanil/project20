package com.ist.sirs.child_locator.handlers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import com.ist.sirs.child_locator.security.CertUtil;
import com.ist.sirs.child_locator.security.CryptoUtil;

import java.io.ByteArrayOutputStream;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.sql.Timestamp;

public class CertClientHandler implements SOAPHandler<SOAPMessageContext> {

	// CLIENT PROPERTIES
	public static final String HandlerName = "CertClientHeader";
	public static final String HandlerPrefix = "CCH";
	public static final String HandlerNamespace = "http://demo";

	// PRIVATE KEY
	final static String KEYSTORE = "client.jks";
	final static String KEYSTORE_PASSWORD = "Spassword";
	final static String KEY_ALIAS = "client";
	final static String KEY_PASSWORD = "Kpassword";
	
	
	//----------------------------------------
	
	// INTEGRITY HANDLER
	public static final String IntegrityHandlerName = "IntegrityHeader";
	public static final String IntegrityHandlerPrefix = "I";
	public static final String IntegrityHandlerNamespace = "http://demo";
	
	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of in bound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) { 
				//System.out.println("[OUTBOUD] Cert Client Handler");
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();				
					
				//Get client private key
				System.out.println("\n\nBEFORE KEYSTORE\n\n");
				PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE,
						KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
				System.out.println("\n\nAFTER KEYSTORE\n\n");
				//Get digest from header of soap envelope
				Name name = se.createName(IntegrityHandlerName, IntegrityHandlerPrefix, IntegrityHandlerNamespace);
				Iterator<?> it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("[CERT CLIENT HANDLER] Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();
				String digest = element.getValue();
				//System.out.println("CERT SERVER HANDLER: DIGEST: "+ digest);
				
				//encrypt digest with server private key
				byte[] base64Digest = DatatypeConverter.parseBase64Binary(digest);
				//System.out.println("CERT SERVER HANDLER: DIGEST64: "+ base64Digest);
				byte[] encryptedBytesDigest = CryptoUtil.asymCipher(base64Digest, privateKey);
				//System.out.println("CERT SERVER HANDLER: ENCRYPTED BYTES: "+ encryptedBytesDigest);
				String encryptedStringDigest = DatatypeConverter.printBase64Binary(encryptedBytesDigest);
				//System.out.println("CERT SERVER HANDLER: ENCRYPTED STRING: "+ encryptedStringDigest);
				
				//put encrypted digest back on the soap header
				element.setTextContent(encryptedStringDigest);
				
			} else {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();	
				
				//get server certificate
				System.out.println("\n\nBEFORE CERTIFICATE\n\n");
				String serverCertName = "server.cer";
				Certificate serverCert = CertUtil.getX509CertificateFromResource(serverCertName);
				System.out.println("\n\nAFTER CERTIFICATE\n\n");
				if(serverCert == null){
					return true;
				}
				
				//get server public key
				PublicKey serverPublicKey = serverCert.getPublicKey();				
				
				//get encrypted digest from soap header
				Name name = se.createName(IntegrityHandlerName, IntegrityHandlerPrefix, IntegrityHandlerNamespace);
				Iterator<?> it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("[CERT CLIENT HANDLER] Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();
				String encryptedDigest = element.getValue();
				//System.out.println("CERT CLIENT HANDLER: ENCRYPTED DIGEST: "+ encryptedDigest);
				
				//decrypt digest with server public key
				byte[] base64EncryptedDigest = DatatypeConverter.parseBase64Binary(encryptedDigest);
				byte[] decryptedBytesDigest = CryptoUtil.asymDecipher(base64EncryptedDigest, serverPublicKey);
				String digest = DatatypeConverter.printBase64Binary(decryptedBytesDigest);
				//System.out.println("CLIENT DIGEST : " + digest+"\n");
				//put decrypted digest back on the soap header
				element.setTextContent(digest);
				
				
				//no need for validation since thats done by IntegrityHandler!
			}

		} catch (Exception e) {
			System.out.print("Caught exception in CertClientHandler: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}
}
