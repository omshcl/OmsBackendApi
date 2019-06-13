package oms;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Api {
	public static Cluster cluster;

	public Api() {
		if(cluster == null) {
			cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		}
	}
	
	public Session getSession() {
		return cluster.connect("oms");
	}
}


