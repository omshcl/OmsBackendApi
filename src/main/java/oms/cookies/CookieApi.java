package oms.cookies;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class CookieApi {
	
	SecureRandom random;
	Map<String,Boolean> keys = new HashMap<>();
	
	public CookieApi() {
		random = new SecureRandom();
	}

	private static final String symbols = "ABCDEFGJKLMNPRSTUVWXYZ0123456789";

	private final char[] buf = new char[40];

	public String getId() {
		for (int idx = 0; idx < buf.length; ++idx) 
	      buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
		String id =  new String(buf);
		System.out.println(id);
		keys.put(id, true);
		return id;
	}
}
