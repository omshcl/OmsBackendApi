package oms.login;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


import oms.Api;
import oms.cookies.CookieApi;

public class CustomerLoginApi extends Api {
	
	private Session session;
	private PreparedStatement user_lookup_stmt, is_admin_stmt;
	private CookieApi cookieApi;
	private SecretKeyFactory keyFact;
	
	public CustomerLoginApi() {
		super();
		session = super.getSession();
		//Selects all user information
		user_lookup_stmt = session.prepare("SELECT *  FROM customer_login where username = ? and password = ? ALLOW FILTERING;");

	}
	
	/**
	 * Checks db to see if user is valid
	 * @param user
	 * @param password
	 * @return boolean
	 */
	public boolean validateUser(String user,String password) {
		String hash = getHash(password);
		System.out.println(hash);
		ResultSet res = session.execute(user_lookup_stmt.bind(user,hash));
		return !res.isExhausted();
	}
	
	/**
	 * Hashes user password using PBKDF2 algorithm which is industry best practie
	 * Also uses a salt value to prevent the use of Rainbow Tables on hashes
	 * @param pass user password
	 * @return the hashed password
	 */
	public String getHash(String pass) {
		// Our salt value
		byte[] salt = "OufjPmEyN8LWM5hfQ7ns".getBytes();
		// pass our password to the keySpec
		KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, 65536,128);
		SecretKeyFactory factory;
		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			byte[] encoded = Base64.getEncoder().encode(hash);
			
			// encode the password hash bytes as a string to store in db
			// explicity states locale to make platform independent
			return new String(encoded);			
		} catch (NoSuchAlgorithmException e) {
			System.out.println("PBKDF2 algorithm not found");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			System.out.println("Invalid Key Spec");
			e.printStackTrace();
		}
		return null;
	}
}
