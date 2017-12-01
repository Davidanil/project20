package com.ist.sirs.child_locator.handlers;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
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

import org.w3c.dom.NodeList;

import java.io.IOException;
import java.time.LocalDateTime;

public class LoginClientTimeHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String HandlerName = "register";
	public static final String HandlerPrefix = "ns2";
	public static final String HandlerNamespace = "http://ws.child_locator.sirs.ist.com/";

	public static final String CONTEXT_PROPERTY = "login_time.property";

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
		System.out.println("TimeHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				System.out.println("Writing header in outbound SOAP message...");

				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = msg.getSOAPBody();
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// add header element (name, namespace prefix, namespace)
				Name name = se.createName(HandlerName, HandlerPrefix, HandlerNamespace);
				
				//if register method was found
				Iterator<?> itr=sb.getChildElements(name);
				if(itr.hasNext()){
					// get phoneNumber from SOAP Body
					Node n = (Node) itr.next();
					NodeList nList = n.getChildNodes();
					String phoneNumber = "";
					int indexNode = 0;
					for(int i = 0; i < nList.getLength(); i++){
						if(nList.item(i).getNodeName().equals("phoneNumber")){
							phoneNumber = nList.item(i).getTextContent();
							indexNode = i;
						}
					}
					
					if(phoneNumber.length() > 0){
						//add it to header
						Name namePhone = se.createName("phoneNumber", "n", "http://phoneNumber");
						SOAPHeaderElement element = sh.addHeaderElement(namePhone);
						element.addTextNode(phoneNumber);
					}
				}
				
			
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
