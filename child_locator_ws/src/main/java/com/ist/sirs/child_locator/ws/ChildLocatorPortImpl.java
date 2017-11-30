package com.ist.sirs.child_locator.ws;

import javax.jws.HandlerChain;
import javax.jws.WebService;

@WebService(
		endpointInterface="com.ist.sirs.child_locator.ws.ChildLocatorPortType",
		wsdlLocation = "childLocator.1_0.wsdl", 
		name = "ChildLocatorService", 
		portName = "ChildLocatorPort", 
		targetNamespace = "http://ws.child_locator.sirs.ist.com/", 
		serviceName = "ChildLocatorService"
		)
//@HandlerChain(file="/child_locator_ws_handler_chain.xml")
public class ChildLocatorPortImpl implements ChildLocatorPortType{
	private ChildLocatorPortImpl() {
		
	}

	private static class SingletonHolder {
		private static final ChildLocatorPortImpl INSTANCE = new ChildLocatorPortImpl();
	}

	public static synchronized ChildLocatorPortImpl getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public String print(){
		return "bazinga";
	}
	
	@Override
	public boolean login(String username, String password){
		return username.equals("sheldon") && password.equals("bazinga");
	}
}
