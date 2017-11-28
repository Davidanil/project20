package com.ist.sirs.child_locator.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;

public class ChildLocatorEndpointManager {
	public static void main (String args[]){
		ChildLocatorPortImpl serverImpl = ChildLocatorPortImpl.getInstance();
		
		Endpoint.publish("http://localhost:19287/ws", serverImpl);
		System.out.println("http://localhost:19287/ws");
		try {
			System.in.read();
		} catch (IOException e) {
			System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
		}
	}
}
