package com.softwareassessment;

import com.softwareassessment.view.ContactListView;
import com.softwareassessment.addressbookForms.NewContactForm;
import com.softwareassessment.addressbookForms.SearchForm;
import com.softwareassessment.utilities.Entry;
import com.softwareassessment.utilities.Redis;
import com.softwareassessment.utilities.User;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.List;

public class Addressbook extends WebPage {
    private static final long serialVersionUID = 2L;
    private ListView listView;
    private User user;

    public Addressbook(final PageParameters parameters) {
        super(parameters);
        String username = parameters.get("username").toString();
        user = Redis.getUserData(username);
        NewContactForm ncf = new NewContactForm("newContactForm", user, this);
        add(ncf);
        add(new FeedbackPanel("feedback").setFilter(new ContainerFeedbackMessageFilter(ncf)));
        add(new SearchForm("searchForm", user, this));
        Entry[] contacts = user.getEntries();
        List<Entry> entryList = Arrays.asList(contacts);
        listView = new ContactListView("listView", entryList, user, this);
        listView.setReuseItems(true);
        add(new FeedbackPanel("updateDeleteFeedback").setFilter(new ContainerFeedbackMessageFilter(listView)));
        add(listView);

        add(new AjaxLink("logout") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getSession().invalidate();
                getRequestCycle().setResponsePage(LoginPage.class);
            }
        });
    }

    public void updateList(Entry[] contacts) {
        List<Entry> entryList = Arrays.asList(contacts);
        listView.removeAll();
        listView.setList(entryList);
    }


}
