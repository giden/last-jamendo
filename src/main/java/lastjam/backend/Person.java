package lastjam.backend;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Person implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8100647323627763490L;
	
	@Id
	private String username;
	
	@NotNull
	@Size(min=5, message="Password is too short")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
