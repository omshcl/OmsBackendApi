package oms;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Api {

	private static Cluster cluster;
	public static Session session;
	int READOUT_TIME=10000000;

	public Api() {
		if(cluster == null) {
			cluster = Cluster.builder().addContactPoint("cassandra").build();
			cluster.getConfiguration().getSocketOptions().setReadTimeoutMillis(READOUT_TIME);
			session = cluster.connect("oms");
		}
	}

	public Session getSession() {
		return session;
	}
}
