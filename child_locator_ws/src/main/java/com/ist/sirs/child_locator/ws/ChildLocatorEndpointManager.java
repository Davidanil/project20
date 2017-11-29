package com.ist.sirs.child_locator.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;

public class ChildLocatorEndpointManager {
	public static void main (String args[]){
		ChildLocatorPortImpl serverImpl = ChildLocatorPortImpl.getInstance();
		
		Endpoint.publish(args[0], serverImpl);
		System.out.println(args[0]);
		try {
			System.in.read();
		} catch (IOException e) {
			System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
		}
	}
}
