package com.ist.sirs.child_locator.ws.cli;

public class ChildLocatorClientApp {

	public static void main(String args[]) {
		
		String wsURL = "http://localhost:19287/ws";
		
		// Create client
		ChildLocatorClient client = null;
		
		client = new ChildLocatorClient(wsURL);
		
		System.out.println("Invoke ping()...");
		System.out.println(client.print());
		System.out.println(client.login("sheldon","bazinga"));
	}
}
