package org.techdesgen;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.internal.jersey.JerseyProjectRestClient;
import com.sun.jersey.client.apache.ApacheHttpClient;

/**
 * User: kcyxa
 * Date: 10/14/12
 * Time: 11:44 PM
 */
public class DesgenProjectSettingsForm {
    private JPanel rootPanel;
    private JTextField siteTextField;
    private JTextField userTextField;
    private JComboBox projectsComboBox;
    private JButton refreshProjectsButton;
    private JPasswordField passwordField;
    private JButton button1;

    public DesgenProjectSettingsForm() {
        refreshProjectsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshProjects();
            }
        });
    }

    private void refreshProjects() {
        URI jiraServerUri = null;
        try {
            jiraServerUri = new URI("http://" + siteTextField.getText());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        //BasicHttpAuthenticationHandler authentication = new BasicHttpAuthenticationHandler(userTextField.getText(), new String(passwordField.getPassword()));
        ProjectRestClient client = new JerseyProjectRestClient(jiraServerUri, new ApacheHttpClient());
        NullProgressMonitor pm = new NullProgressMonitor();
        for (BasicProject project: client.getAllProjects(pm) ) {
            projectsComboBox.addItem(project.getName());
        }

    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public boolean isModified(DesgenProjectSettings settings) {
        return !settings.getJiraProject().equals(projectsComboBox.getSelectedItem());
    }

    public void getData(DesgenProjectSettings settings) {
        settings.setJiraProject(projectsComboBox.getSelectedItem().toString());
    }

    public void setData(DesgenProjectSettings settings) {
        projectsComboBox.setSelectedItem(settings.getJiraProject());
    }
}
