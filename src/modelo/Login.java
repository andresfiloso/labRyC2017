package modelo;

import java.io.Serializable;

class Login implements Serializable {
	private String user;
	private String pass;

	public Login(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}

	public Login() {
		this.user = "unlogged";
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}
}
