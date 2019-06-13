package oms.login;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import oms.Api;

public class LoginApi extends Api {
	
	private Session session;
	private PreparedStatement user_lookup_stmt, is_admin_stmt;

	public LoginApi() {
		super();
		session = super.getSession();
		user_lookup_stmt = session.prepare("SELECT *  FROM users where username = ? and password = ? ALLOW FILTERING;");
		is_admin_stmt    = session.prepare("SELECT isAdmin from users where username = ?");
	}
	
	public boolean validateUser(String user,String password) {
		ResultSet res = session.execute(user_lookup_stmt.bind(user,password));
		return !res.isExhausted();
	}
	
	public boolean isAdmin(String user) {
		ResultSet res = session.execute(is_admin_stmt.bind(user));
		Row row = res.one();
		if (row == null) {
			return false;
		}
		return row.getBool("isadmin");
	}
}
