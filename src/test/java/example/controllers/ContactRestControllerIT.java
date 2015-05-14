package example.controllers;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.gson.Gson;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import example.Application;
import example.SecurityConfiguration;
import example.models.Contact;
import example.repositories.ContactRepository;

/**
 * See test document [ContactRestController Integration
 * Testing](tests/ContactRestController.java) for test details.
 * 
 * @author ole
 */
@RunWith(SpringJUnit4ClassRunner.class)
// @SpringApplicationConfiguration(classes = Application.class)
@SpringApplicationConfiguration(classes = {
    Application.class, SecurityConfiguration.class
})
@WebAppConfiguration
@IntegrationTest({
    "server.port=0"
})
@DirtiesContext
public class ContactRestControllerIT {

    @Autowired
    ContactRepository CR;

    private Gson gson = new Gson();

    @Value("${local.server.port}")
    private int port;

    private final String USER = "user";

    private final String ADMIN = "admin";

    private final String PASSWORD = "password";

    private final String contactRestPath = "/api/v1/contact/";

    private final String contactsAllRestPath = "/api/v1/contacts";

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    private Contact createValidContact() {
        return Contact.builder()
            .firstName("Ole")
            .lastName("Ersoy")
            .email("ole.ersoy@gmail.com")
            .build();
    }

    private Contact validContact = createValidContact();

    @Test
    public void postInvalidContact() {
        String firstNameJson = "{\"firstName\":\"Ole\"}";

        Response response =
            given().auth()
                .basic(USER, PASSWORD)
                .contentType("application/json")
                .body(firstNameJson)
                .when()
                .post(contactRestPath);

        response.then().statusCode(HttpStatus.BAD_REQUEST.value());

        String body = response.getBody().asString();

        // This will mention the fields are that invalid.
        // Look for these fields through the testing code
        // Should create a Data Transfer Object for the invalid
        // fields and return that.

        System.out.println("\n\n\n\n" + body + "\n\n\n\n");

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertThat(CR.findAll().size(), is(0));
    }

    @Test
    public void postValidContact() {

        Response response =
            given().auth()
                .basic(USER, PASSWORD)
                .contentType("application/json")
                .body(gson.toJson(validContact))
                .when()
                .post(contactRestPath);

        String contactJson = response.getBody().asString();
        Contact received = gson.fromJson(contactJson, Contact.class);

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        // The id is not null since it was saved to the database.
        assertTrue(!Objects.isNull(received.getId()));
        assertThat(CR.findAll().size(), is(1));

        CR.deleteAll();
    }

    @Test
    public void updateContact() {

        Response response =
            given().auth()
                .basic(ADMIN, PASSWORD)
                .contentType("application/json")
                .body(gson.toJson(validContact))
                .when()
                .post(contactRestPath);

        String contactJson = response.getBody().asString();
        Contact received = gson.fromJson(contactJson, Contact.class);

        // The id is not null since it was saved to the database.
        assertTrue(!Objects.isNull(received.getId()));
        assertThat(received.getFirstName(), is(validContact.getFirstName()));

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertThat(CR.findAll().size(), is(1));

        received.setFirstName("Test");
        given().auth()
            .basic(ADMIN, PASSWORD)
            .contentType("application/json")
            .body(gson.toJson(received))
            .when()
            .post(contactRestPath);

        Contact update = CR.findOne(received.getId());

        assertThat(update.getFirstName(), is(received.getFirstName()));

        CR.deleteAll();
    }

    @Test
    public void getValidContact() {
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(ADMIN,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        Contact contact = CR.save(validContact);
        given().auth()
            .basic(ADMIN, PASSWORD)
            .get(contactRestPath + "{id}", contact.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("firstName", is(contact.getFirstName()))
            .body("id", is(contact.getId().intValue()));

        CR.deleteAll();
    }

    @Test
    public void getInvalidContact() {

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(USER,
                                                                       PASSWORD,
                                                                       Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        Contact contact = CR.save(validContact);
        given().auth()
            .basic(ADMIN, PASSWORD)
            .get(contactRestPath + "{id}", contact.getId() + 10L)
            .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        CR.deleteAll();
    }

    @Test
    public void getInvalidValidContact2() {

        given().auth()
            .basic(ADMIN, PASSWORD)
            .get(contactRestPath + "{id}", 1L)
            .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}
