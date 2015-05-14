package example.repositories;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import example.Application;
import example.models.Contact;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class ContactRepositoryTest {

    @Autowired
    ContactRepository CR;

    private static final String USER = "user";

    private static final String ADMIN = "admin";

    private static final String PASSWORD = "password";

    private Contact create() {
        return Contact.builder()
            .firstName("Ole")
            .lastName("Ersoy")
            .email("ole.ersoy@gmail.com")
            .build();
    }

    /*
     * When using the Validator, a ConstraintViolationException is triggered if
     * any of the contact properties are invalid. However this is not true for
     * all in general for the Repositories. See
     * https://jira.spring.io/browse/DATAJPA-718
     */
    @Test(expected = ConstraintViolationException.class)
    public void constraintViolationException1() {
        Contact contact =
            Contact.builder()
                .firstName("Ole")
                .lastName("Ersoy")
                .email("ole")
                .build();

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(USER,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));

        CR.save(contact);
    }

    @Test
    public void crud() {
        Contact contact = create();

        assertThat(contact.getId(), is(nullValue()));
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(USER,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
        CR.save(contact);

        assertThat(contact.getId(), is(not(nullValue())));

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        CR.deleteAll();
        List<Contact> instances = CR.findAll();
        assertThat(instances.size(), is(0));
    }
}
