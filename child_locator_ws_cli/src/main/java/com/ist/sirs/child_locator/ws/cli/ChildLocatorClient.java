package com.ist.sirs.child_locator.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
    public String print() throws InvalidLoginTime_Exception{
		return port.print();
	}

	@Override
	public String login(String phoneNumber, String email, String password) 
			throws InvalidPhoneNumber_Exception, InvalidEmail_Exception, InvalidPassword_Exception{
		return port.login(phoneNumber, email, password);
	}
	
	@Override
	public boolean confirmLogin(String code){
		return port.confirmLogin(code);
	}

	public boolean sendCoordinates(String phone, String latitude, String longitude) {
		return port.sendCoordinates(phone, latitude, longitude);
	}
	
	@Override
	public String getCoordinates(String phoneDad, String phoneSon) {
		return port.getCoordinates(phoneDad, phoneSon);
	}
	
	@Override
	public String register(String phoneNumber, String email, String password1, String password2) 
			throws InvalidPhoneNumber_Exception, InvalidEmail_Exception, InvalidPassword_Exception, DifferentPasswords_Exception{
		return port.register(phoneNumber, email, password1, password2);
	}
	
	@Override
	public boolean confirmRegistration(String code){
		return port.confirmRegistration(code);
	}
	
	@Override
	public String createChannel(String phoneNumber, String publicKey) throws InvalidPhoneNumber_Exception, ConnectionAlreadyExists_Exception{
		return port.createChannel(phoneNumber, publicKey);
	}
	
	@Override
	public List<FolloweeView> getFollowees(){
		return port.getFollowees();
	}
	
	@Override
	public List<String> getFollowers(){
		return port.getFollowers();
	}
	
	@Override
	public String getAddNonce(String phoneNumber)  
			throws ConnectionAlreadyExists_Exception, InvalidPhoneNumber_Exception{
		return port.getAddNonce(phoneNumber);
	}
	
	@Override
	public boolean addFollowee(String phoneNumber, String nonce) throws InvalidPhoneNumber_Exception, InvalidNonce_Exception{
		return port.addFollowee(phoneNumber, nonce);
	}
}
