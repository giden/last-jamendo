package lastjam.backend;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

@Entity
public class Band implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8646110752797604059L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Temporal(TemporalType.DATE)
    private Date formed;

    @NotNull(message = "Name is required")
    @Size(min = 3, max = 50, message = "name must be longer than 3 and less than 40 characters")
    private String name;

    @URL
    private String website;
    
    public Band() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getFormed() {
        return formed;
    }

    public void setFormed(Date formed) {
        this.formed = formed;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
