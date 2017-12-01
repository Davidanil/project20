package com.ist.sirs.child_locator.ws;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.ws.Endpoint;

import com.ist.sirs.child_locator.ws.db.ChildLocatorDB;


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
