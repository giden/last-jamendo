package lastjam.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Empty JpaRepository is enough for a simple crud.
 */
public interface BandRepository extends JpaRepository<Band, Long> {
	public List<Band> findByPerson(Person p);
	public Band findById(long id);
}
