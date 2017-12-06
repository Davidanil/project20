package com.ist.sirs.child_locator.handlers;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class TimeHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String HandlerName = "TimeHeader";
	public static final String HandlerPrefix = "T";
	public static final String HandlerNamespace = "http://demo";

	
	public static final String CONTEXT_PROPERTY = "time.property";

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
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		//System.out.println("TimeHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) { // Add time stamp header
				//System.out.println("Writing header in outbound SOAP message...");
				
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(HandlerName, HandlerPrefix, HandlerNamespace);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				
				// add header element value
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String valueString = timestamp.toString();
				element.addTextNode(valueString);
			}
			else{ // Check time stamp
				//System.out.println("Reading header in inbound SOAP message...");
				
				// get SOAP envelope header
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}

				// get first header element
				Name name = se.createName(HandlerName, HandlerPrefix, HandlerNamespace);
				Iterator<?> it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();

				// get header element value
				String valueString = element.getValue();

				Timestamp timestamp = Timestamp.valueOf(valueString);
				// put header in a property context
				smc.put(CONTEXT_PROPERTY, timestamp);
				// set property scope to application client/server class can
				// access it
				smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);

				int seconds = 0;
				Properties prop = new Properties();
				try{
					prop.load(TimeHandler.class.getResourceAsStream("/config.properties"));
					seconds = Integer.parseInt(prop.getProperty("freshnessTimeout"));
				} catch(IOException ioe){
					System.out.println(ioe);
				}

				//Add x seconds to timestamp
				Timestamp interval = new Timestamp(timestamp.getTime() + seconds * 1000);

				if(interval.before(new Timestamp(System.currentTimeMillis()))) // time > X sec, error
				   throw new RuntimeException(String.format("Time exceeded %d seconds.",seconds));
            
			}
		} catch (Exception e) {
			System.out.print("Caught exception in TimeHandler: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
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
