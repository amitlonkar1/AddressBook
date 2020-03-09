package com.softwareassessment.addressbookForms;

import com.softwareassessment.Addressbook;
import com.softwareassessment.utilities.Redis;
import com.softwareassessment.utilities.User;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

public class SearchForm extends Form{
    private Addressbook addressbook;
    private TextField searchInput;
    private Button searchButton, clearButton;
    private User user;

    public SearchForm(String id, User user, Addressbook addressbook) {
        super(id);
        this.addressbook = addressbook;
        this.user = user;

        searchInput = new TextField("searchInput", Model.of(""));

        searchButton = new Button("searchButton") {
            public void onSubmit() {
                search();
            }
        };
        clearButton = new Button("clearSearchButton") {
            public void onSubmit() {
                searchInput.setDefaultModelObject("");
                resetContacts();
            }
        };
        add(searchInput);
        add(searchButton);
        add(clearButton);
    }

    //Initiates a search and updates the list with the returned arrays

    private void search() {
        addressbook.updateList(Redis.searchEntries(user, searchInput.getInput()));
    }

    // Clears the search and resets the table back to the full list of contacts

    private void resetContacts() {
        addressbook.updateList(user.getEntries());
    }

}
