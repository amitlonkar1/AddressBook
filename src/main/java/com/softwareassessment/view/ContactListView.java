package com.softwareassessment.view;

import com.softwareassessment.Addressbook;
import com.softwareassessment.addressbookForms.ContactUpdateForm;
import com.softwareassessment.utilities.Entry;
import com.softwareassessment.utilities.User;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.util.List;

public class ContactListView extends ListView{
    TextField firstNameField, lastNameField, addressField, emailField, phoneField;
    private Button updateButton, deleteButton;
    List list;
    User user;
    Addressbook addressbook;

    public ContactListView(String id, List list, User user, Addressbook addressbook) {
        super(id, list);
        this.list = list;
        this.user = user;
        this.addressbook = addressbook;
    }

    @Override
    protected void populateItem(final ListItem item) {
        final Entry entry = (Entry) item.getModelObject();
        item.add(new ContactUpdateForm("contactUpdateForm", entry, user, addressbook));
    }
}
