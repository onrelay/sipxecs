package org.sipfoundry.sipxconfig.rest;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.sipfoundry.commons.rest.XStreamRepresentation;
import org.sipfoundry.sipxconfig.common.BeanWithId;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.phonebook.AddressBookEntry;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.Collection;

public class UserPhonebookEntryResource extends UserResource {

    private PhonebookManager m_phonebookManager;
    private String m_entryId;

    @Override
    protected void doInit() {
        m_entryId = (String) getRequest().getAttributes().get("entryId");
        // Define supported media types for representations
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Get
    public Representation represent(Variant variant) throws ResourceException {
        Phonebook privatePhonebook = m_phonebookManager.getPrivatePhonebook(getUser());
        if (privatePhonebook != null) {
            Collection<PhonebookEntry> entries = privatePhonebook.getEntries();
            ArrayList<String> ids = new ArrayList<>();
            for (PhonebookEntry entry : entries) {
                ids.add(String.valueOf(entry.getId()));
            }
            if (ids.contains(m_entryId)) {
                PhonebookEntry entry = m_phonebookManager.getPhonebookEntry(Integer.parseInt(m_entryId));
                PhonebookEntry reprEntry = (PhonebookEntry) entry.duplicate();
                return new PhonebookEntryRepresentation(variant.getMediaType(), reprEntry);
            }
        }
        return null;
    }

    @Post
    public Representation acceptRepresentation(Representation entity) throws ResourceException {
        PhonebookEntryRepresentation representation = new PhonebookEntryRepresentation(entity);
        PhonebookEntry newEntry = representation.getObject();
        if (!validatePhonebookEntry(newEntry)) {
            return null;
        }

        if (m_phonebookManager.getDuplicatePhonebookEntry(newEntry, getUser()) != null) {
            setDuplicateEntryStatus();
            return null;
        }

        User user = getUser();
        Phonebook privatePhonebook = m_phonebookManager.getPrivatePhonebookCreateIfRequired(user);
        newEntry.setPhonebook(privatePhonebook);
        privatePhonebook.getEntries().add(newEntry);
        m_phonebookManager.savePhonebook(privatePhonebook);
        return new PhonebookEntryRepresentation(MediaType.APPLICATION_JSON, newEntry);
    }

    @Put
    public Representation storeRepresentation(Representation entity) throws ResourceException {
        PhonebookEntryRepresentation representation = new PhonebookEntryRepresentation(entity);
        PhonebookEntry newEntry = representation.getObject();
        if (!validatePhonebookEntry(newEntry)) {
            return null;
        }

        PhonebookEntry duplicateEntry = m_phonebookManager.getDuplicatePhonebookEntry(newEntry, getUser());
        if (duplicateEntry != null && !duplicateEntry.getId().equals(Integer.parseInt(m_entryId))) {
            setDuplicateEntryStatus();
            return null;
        }

        Phonebook privatePhonebook = m_phonebookManager.getPrivatePhonebook(getUser());
        if (privatePhonebook != null) {
            newEntry.setPhonebook(privatePhonebook);
            PhonebookEntry entry = m_phonebookManager.getPhonebookEntry(Integer.parseInt(m_entryId));
            entry.update(newEntry);
            m_phonebookManager.updatePhonebookEntry(entry);
            return new PhonebookEntryRepresentation(MediaType.APPLICATION_JSON, newEntry);
        }
        return null;
    }

    @Delete
    public void removeRepresentations() throws ResourceException {
        Phonebook privatePhonebook = m_phonebookManager.getPrivatePhonebook(getUser());
        if (privatePhonebook != null) {
            Collection<PhonebookEntry> entries = privatePhonebook.getEntries();
            ArrayList<String> ids = new ArrayList<>();
            for (PhonebookEntry entry : entries) {
                ids.add(String.valueOf(entry.getId()));
            }
            if (ids.contains(m_entryId)) {
                PhonebookEntry entry = m_phonebookManager.getPhonebookEntry(Integer.parseInt(m_entryId));
                m_phonebookManager.deletePhonebookEntry(entry);
            }
        }
    }

    private void setDuplicateEntryStatus() {
        getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Duplicate Entry");
    }

    private boolean validatePhonebookEntry(PhonebookEntry newEntry) {
        // validate given values - first name, last name, number and e-mail (if provided)
        if (StringUtils.isEmpty(newEntry.getFirstName()) || StringUtils.isEmpty(newEntry.getLastName())
                || StringUtils.isEmpty(newEntry.getNumber())) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                    "First Name / Last Name / Number cannot be Null");
            return false;
        }

        if (!StringUtils.isEmpty(newEntry.getAddressBookEntry().getEmailAddress())) {
            // validate email format
            if (!isValidEmail(newEntry.getAddressBookEntry().getEmailAddress())) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid Email Address");
                return false;
            }
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");
    }

    protected User getUser() {
        // Assume we have a user context here, replace with actual logic for getting the current user.
        return new User();
    }

    public void setPhonebookManager(PhonebookManager phonebookManager) {
        m_phonebookManager = phonebookManager;
    }

    static class PhonebookEntryRepresentation extends XStreamRepresentation<PhonebookEntry> {
        public PhonebookEntryRepresentation(MediaType mediaType, PhonebookEntry object) {
            super(mediaType, object);
        }

        public PhonebookEntryRepresentation(Representation representation) {
            super(representation);
        }

        @Override
        protected void configureXStream(XStream xstream) {
            xstream.omitField(BeanWithId.class, "m_id");
            xstream.alias("entry", PhonebookEntry.class);
            xstream.aliasField("first-name", PhonebookEntry.class, "firstName");
            xstream.aliasField("last-name", PhonebookEntry.class, "lastName");
            xstream.aliasField("contact-information", PhonebookEntry.class, "addressBookEntry");
            xstream.omitField(PhonebookEntry.class, "m_phonebook");
            xstream.omitField(AddressBookEntry.class, "m_useBranchAddress");
            xstream.omitField(AddressBookEntry.class, "m_branchOfficeAddress");
        }
    }
}