package oms.login;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


import oms.Api;
import oms.cookies.CookieApi;

public class LoginApi extends Api {
	
	private Session session;
	private PreparedStatement user_lookup_stmt, is_admin_stmt;
	private CookieApi cookieApi;
	private SecretKeyFactory keyFact;
	
	public LoginApi() {
		super();
		session = super.getSession();
		//Selects all user information
		user_lookup_stmt = session.prepare("SELECT *  FROM users where username = ? and password = ? ALLOW FILTERING;");
		//Gets admin status given username
		is_admin_stmt    = session.prepare("SELECT isAdmin from users where username = ?");
	}
	
	/**
	 * Checks db to see if user is valid
	 * @param user
	 * @param password
	 * @return boolean
	 */
	public boolean validateUser(String user,String password) {
		ResultSet res = session.execute(user_lookup_stmt.bind(user,password));
		String hash = getHash(password);
		System.out.println("HASH");
		System.out.println(hash);
		return !res.isExhausted();
	}
	
	/**
	 * Checks db to see if user is an admin
	 * @param user
	 * @return boolean
	 */
	public boolean isAdmin(String user) {
		ResultSet res = session.execute(is_admin_stmt.bind(user));
		Row row = res.one();
		if (row == null) {
			return false;
		}
		return row.getBool("isadmin");
	}
	
	public void closeSession() {
		session.close();
	}
	
	/**
	 * Hashes user password uses PBKDF2 algorithm which is industry best practices
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
			return hash.toString();
			
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
