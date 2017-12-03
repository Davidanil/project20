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

public class LoginRegisterIdHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String CONTEXT_PROPERTY = "login_time.property";
	
	private String phoneNumber = "";
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
		//System.out.println("LoginRegisterIdHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if (outboundElement.booleanValue()) {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = msg.getSOAPBody();
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();

				// if register method was found
				Name nameRegister = se.createName("registerResponse", "ns2", "http://ws.child_locator.sirs.ist.com/");
				Iterator<?> itrRegister = sb.getChildElements(nameRegister);
				if (itrRegister.hasNext()) {
					// get phoneNumber from SOAP Body
					Node n = (Node) itrRegister.next();
					NodeList nList = n.getChildNodes();
					boolean returnValue = false;
					
					for (int i = 0; i < nList.getLength(); i++) {
						if (nList.item(i).getNodeName().equals("return")) {
							String returnValStr = nList.item(i).getTextContent();
							returnValue = returnValStr.equals("true") ? true : false;
						}
					}
					if(returnValue){
						//add phoneNumber to header
						//System.err.println("[OUTBOUND] PHONE NUMBER: " + this.phoneNumber);
						
						// add header element (name, namespace prefix, namespace)
						Name name = se.createName("phoneNumber", "ph", "http://bazinga");
						SOAPHeaderElement element = sh.addHeaderElement(name);
						// add header element value
						element.addTextNode(phoneNumber);
						
						return true;
					}
				}
				
				//if user is trying to login
				Name nameLogin = se.createName("loginResponse", "ns2", "http://ws.child_locator.sirs.ist.com/");
				Iterator<?> itrLogin = sb.getChildElements(nameLogin);
				if (itrLogin.hasNext()) {
					// get phoneNumber from SOAP Body
					Node n = (Node) itrLogin.next();
					NodeList nList = n.getChildNodes();
					boolean returnValue = false;

					for (int i = 0; i < nList.getLength(); i++) {
						if (nList.item(i).getNodeName().equals("return")) {
							String returnValStr = nList.item(i).getTextContent();
							returnValue = returnValStr.equals("true") ? true : false;
						}
					}
					if(returnValue){
						//add phoneNumber to header
						//System.err.println("[OUTBOUND] PHONE NUMBER: " + this.phoneNumber);
						
						// add header element (name, namespace prefix, namespace)
						Name name = se.createName("phoneNumber", "ph", "http://bazinga");
						SOAPHeaderElement element = sh.addHeaderElement(name);
						// add header element value
						element.addTextNode(phoneNumber);
						
						return true;
					}
				}
			} else {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPBody sb = msg.getSOAPBody();

				// if register method was found
				Name nameRegister = se.createName("register", "ns2", "http://ws.child_locator.sirs.ist.com/");
				Iterator<?> itrRegister = sb.getChildElements(nameRegister);
				if (itrRegister.hasNext()) {
					// get phoneNumber from SOAP Body
					Node n = (Node) itrRegister.next();
					NodeList nList = n.getChildNodes();
					int indexNode = 0;
					for (int i = 0; i < nList.getLength(); i++) {
						if (nList.item(i).getNodeName().equals("phoneNumber")) {
							this.phoneNumber = nList.item(i).getTextContent();
							break;
						}
					}
				}
				
				//if user is trying to login
				Name nameLogin = se.createName("login", "ns2", "http://ws.child_locator.sirs.ist.com/");
				Iterator<?> itrLogin = sb.getChildElements(nameLogin);
				if (itrLogin.hasNext()) {
					// get phoneNumber from SOAP Body
					Node n = (Node) itrLogin.next();
					NodeList nList = n.getChildNodes();

					int indexNode = 0;
					for (int i = 0; i < nList.getLength(); i++) {
						if (nList.item(i).getNodeName().equals("phoneNumber")) {
							this.phoneNumber = nList.item(i).getTextContent();
							break;
						}
					}
				}
				
				
				// put header in a property context
				smc.put(CONTEXT_PROPERTY, this.phoneNumber);
				// set property scope to application client/server class can
				// access it
				smc.setScope(CONTEXT_PROPERTY, Scope.APPLICATION);
				
			}
		} catch (Exception e) {
			System.out.print("Caught exception in LoginRegisterIdHandler: ");
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
