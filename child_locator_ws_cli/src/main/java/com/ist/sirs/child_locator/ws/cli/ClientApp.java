package com.ist.sirs.child_locator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import com.ist.sirs.child_locator.ws.*;

public class ClientApp implements ChildLocatorPortType{
	public static ChildLocatorPortType port;
	public static void main(String args[]) {

//		try {
//
//			URL url;
//
//			url = new URL("http://localhost:19287?wsdl");
//
//			// 1st argument service URI, refer to wsdl document above
//			// 2nd argument is service name, refer to wsdl document above
//			QName qname = new QName("http://ws.child_locator.sirs.ist.com/", "ServerImplService");
//			Service service = Service.create(url, qname);
//			Server server = service.getPort(Server.class);
//			System.out.println(server.print());
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
		
		String wsURL = "http://localhost:19287/ws";
		
		ChildLocatorService service = new ChildLocatorService();
		port = service.getChildLocatorPort();
		
		BindingProvider bindingProvider = (BindingProvider) port;
    	Map<String, Object> requestContext = bindingProvider
    			.getRequestContext();
    	requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
    	
    	new ClientApp().print();
	}

	
	public String print() {
		System.out.println("---TROLOLOLOLOLOL---");
		System.out.println(port.print());
		System.out.println("---TROLOLOLOLOLOL---");
		return null;
	}
}
