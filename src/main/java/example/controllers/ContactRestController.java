package example.controllers;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import example.models.Contact;
import example.repositories.ContactRepository;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ContactRestController {

    private static final String ENTITY_NOT_FOUND_EXCEPTION_TEMPLATE =
        "The contact with id %d could not be found";

    @Autowired
    ContactRepository CR;

    /**
     * Create a new contact entity
     * 
     * @param contact
     * @return the saved Contact instance
     */
    @RequestMapping(value = "/contact", method = RequestMethod.POST)
    public @ResponseBody Contact create(@Valid @RequestBody Contact contact) {
        log.trace("Saving contact {}", contact);
        CR.save(contact);
        return contact;
    }

    /**
     * Update a Contact entity.
     * 
     * @param contact
     * @return the updated Contact instance
     */
    @RequestMapping(value = "/contact", method = RequestMethod.PUT)
    public Contact update(@Valid @RequestBody Contact contact) {
        log.trace("Updating contact {}", contact);
        CR.save(contact);
        return contact;
    }

    /**
     * Query the ContactRepository for a single contact.
     * 
     * @param id
     * @return the Contact instance matching the id or null.
     */
    @RequestMapping(value = "/contact/{id}", method = RequestMethod.GET)
    public Contact read(@PathVariable("id") Long id) {

        log.trace("Retrieving contact for id {}", id);
        Contact contact = CR.findOne(id);
        if (Objects.isNull(contact)) {
            throw new EntityNotFoundException(String.format(ENTITY_NOT_FOUND_EXCEPTION_TEMPLATE,
                                                            id));
        }
        return contact;
    }

    /**
     * Delete contact corresponding to id parameter
     * 
     * @param id
     */
    @RequestMapping(value = "/contact/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable("id") Long id) {
        CR.delete(id);
    }

    /**
     * Query the ContactsRepository for all contacts.
     * 
     * @return all contacts
     */
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public List<Contact> readAll() {
        log.trace("Retrieving all contacts.");
        return CR.findAll();
    }
}
