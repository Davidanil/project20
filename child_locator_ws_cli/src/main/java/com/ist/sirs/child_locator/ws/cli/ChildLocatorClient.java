package com.ist.sirs.child_locator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.ws.BindingProvider;

import com.ist.sirs.child_locator.ws.*;

public class ChildLocatorClient implements ChildLocatorPortType{
	// /** WS service */
	ChildLocatorService service = null;

    // /** WS port (port type is the interface, port is the implementation) */
	ChildLocatorPortType port = null;
	
	private String wsURL = null;
	
	private boolean verbose = false;
	
	/** constructor with provided web service URL */
    public ChildLocatorClient(String wsURL) {
        this.wsURL = wsURL;
        
        createStub();
    }
    
    /** Stub creation and configuration */
    private void createStub() {
        if (verbose)
        	System.out.println("Creating stub ...");

        service = new ChildLocatorService();
        port = service.getChildLocatorPort();

        if (wsURL != null) {
        	if (verbose)
        		System.out.println("Setting endpoint address ...");
        	BindingProvider bindingProvider = (BindingProvider) port;
        	Map<String, Object> requestContext = bindingProvider
        			.getRequestContext();
        	requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
        }
    }
	
 // remote invocation methods ----------------------------------------------    
	@Override
    public String print() {
		return port.print();
	}

	@Override
	public boolean login(String username, String password){
		return port.login(username, password);
	}
}
