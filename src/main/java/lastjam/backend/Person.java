package lastjam.backend;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

@Entity
public class Person implements Serializable {


	private static final long serialVersionUID = -8100647323627763490L;
	
	@Id
	@Email
	@NotNull
	private String username;
	
	@NotNull
	@Size(min=5, message="Password is too short")
	private String password;
	
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Set<Band> bands;

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

	public Set<Band> getBands() {
		return bands;
	}

	public void setBands(Set<Band> bands) {
		this.bands = bands;
	}
	public void removeBand(Band band)
	{
	    this.bands.remove(band);
	}
	

}
