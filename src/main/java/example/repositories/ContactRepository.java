package example.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import example.models.Contact;

@PreAuthorize("hasRole('ROLE_ADMIN')")
public interface ContactRepository
    extends JpaRepository<Contact, Long> {

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    Contact save(Contact contact);
}
