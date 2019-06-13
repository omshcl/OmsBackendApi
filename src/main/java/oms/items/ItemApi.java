package oms.items;

import com.datastax.driver.core.Session;

import oms.Api;

public class ItemApi extends Api {
	
	private Session session;
	
	public ItemApi() {
		super();
		session = super.getSession();
	}
	
	
	
}
