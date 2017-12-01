package com.ist.sirs.child_locator.handlers;

import java.util.Iterator;
import java.util.Set;

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

import java.io.ByteArrayOutputStream;

import java.security.MessageDigest;


/**
 * This SOAPHandler outputs the contents of in bound and out bound messages.
 */
public class IntegrityHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String HandlerName = "IntegrityHeader";
	public static final String HandlerPrefix = "I";
	public static final String HandlerNamespace = "http://demo";

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
		handleIntegrity(smc);
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

	private void handleIntegrity(SOAPMessageContext smc) {
		System.out.println("IntegrityHandler: Handling message.");

		byte[] message = null;
		MessageDigest md = null;
		try {
			message = getBody(smc).getBytes("UTF-8");
			md = MessageDigest.getInstance("MD5");
		} catch (Exception e) {System.out.println(e);}
		byte[] hash = md.digest(message); // hashed
		
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if (outbound){	// Add Hash
			AddHeader(smc, hash.toString());
		}
		else if(!CompareHash(smc, hash.toString())){ // Compare hash
			// Error
		}
	}
	
	private String getBody(SOAPMessageContext smc){ // returns content of Body in a SOAP message
		int i,j;
		String message = "";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try { // convert SOAP object to text
			smc.getMessage().writeTo(stream);
			message = new String(stream.toByteArray(), "utf-8");
		} catch (Exception e) {System.out.println(e);}
		
		for(i = 0; !message.substring(i, i+4).equals("Body"); i++); // find beginning of Body
		i = i + 5; // <S:Body> go from B to content ->
		for(j = i; !message.substring(j, j+4).equals("Body"); j++); // find end of Body
		j = j - 4; // </S:Body> go from B to content <-
		return message.substring(i, j);
	}
	
	private void AddHeader(SOAPMessageContext smc, String hash){ // Add hash header to message
		System.out.println("Adding message hash.");
		
		// get SOAP envelope
		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		SOAPHeader sh = null;
		SOAPEnvelope se = null;
		Name name = null;

		try {
			se = sp.getEnvelope();
			sh = se.getHeader();
			if (sh == null)
				sh = se.addHeader();
			name = se.createName(HandlerName, HandlerPrefix, HandlerNamespace);
			SOAPHeaderElement element = sh.addHeaderElement(name);
			// add header hash
			element.addTextNode(hash);
		} catch (Exception e) {System.out.println(e);}		
	}
	
	private boolean CompareHash(SOAPMessageContext smc, String hash){ // read hash from message, returns true if equal
		System.out.println("Reading message hash.");
		
		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		SOAPHeader sh = null;
		SOAPEnvelope se = null;
		Name name = null;

		try {
			se = sp.getEnvelope();
			sh = se.getHeader();
			if (sh == null) {
				System.out.println("Header not found.");
				return false;
			}
			name = se.createName(HandlerName, HandlerPrefix, HandlerNamespace);
		} catch (Exception e) {System.out.println(e);}

		Iterator<?> it = sh.getChildElements(name);
		// check header element
		if (!it.hasNext()) {
			System.out.println("Header element not found.");
			return false;
		}
		SOAPElement element = (SOAPElement) it.next();
		// get hash
		String valueString = element.getValue();
		// compare hashes
		return valueString.equals(hash);
	}

}
