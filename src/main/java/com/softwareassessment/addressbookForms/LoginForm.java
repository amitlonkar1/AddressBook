package com.softwareassessment.addressbookForms;

import com.softwareassessment.Addressbook;
import com.softwareassessment.utilities.Redis;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;


public class LoginForm extends Form {

    private TextField usernameField;
    private TextField passwordField;
    private Button submitButton, registerButton;
    private Label loginStatus;

    public LoginForm(String id) {
        super(id);

        usernameField = new TextField("username", Model.of(""));
        passwordField = new PasswordTextField("password", Model.of(""));
        loginStatus = new Label("loginStatus", Model.of(""));
        submitButton = new Button("submitButton") {
            public void onSubmit() {
                attemptLogin();
            }
        };
        registerButton = new Button("registerButton") {
            public void onSubmit() {
                registerUser();
            }
        };
        add(usernameField);
        add(passwordField);
        add(loginStatus);
        add(submitButton);
        add(registerButton);
    }

    private void registerUser() {
        String username = usernameField.getInput();
        String password = passwordField.getInput();
        if (Redis.registerUser(username, password)) {
            loginStatus.setDefaultModelObject("Successfully Registered");
        } else {
            loginStatus.setDefaultModelObject("That username is already in use.");
        }
    }

    private void attemptLogin() {
        String username = usernameField.getInput();
        String password = passwordField.getInput();
        if (Redis.login(username, password)) {
            loginStatus.setDefaultModelObject("LoginSuccessful");
            PageParameters parameters = new PageParameters();
            parameters.add("username", username);
            setResponsePage(new Addressbook(parameters));
        } else {
            loginStatus.setDefaultModelObject("Wrong username or password");
        }
    }
}
