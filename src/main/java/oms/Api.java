   

package oms;





import com.datastax.driver.core.Cluster;

import com.datastax.driver.core.Session;



public class Api {

	public static Cluster cluster;
	int READOUT_TIME=10000000;


	public Api() {
		if(cluster == null) {
			cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
			cluster.getConfiguration().getSocketOptions().setReadTimeoutMillis(READOUT_TIME);
		}
	}

	

	public Session getSession() {

		return cluster.connect("oms");

	}

}