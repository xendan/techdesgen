package org.techdesgen;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: kcyxa
 * Date: 10/14/12
 * Time: 11:44 PM
 * To change this template use File | Settings | File Templates.
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
        projectsComboBox.addItem("Super project");
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
