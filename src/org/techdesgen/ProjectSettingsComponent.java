/*
 * Copyright (c) 2009, David A. Freels Sr.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 * the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.techdesgen;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Image;
import java.util.Timer;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * This is the main entry point.
 *
 * @author David A. Freels Sr.
 */
@State(name = "TechDesGenProjectSettingsComponent",
        storages = @Storage(id = "ThecDesGenProjectSettingsComponent", file = "$PROJECT_FILE$"))
public class ProjectSettingsComponent implements ProjectComponent, Configurable, PersistentStateComponent<DesgenProjectSettings>
{
    public static final String TOOL_WINDOW_ID = "Tech Design Generator";
    public static final String CONSOLE_WINDOW_ID = "Tech Design Generator Information";

    private DesgenProjectSettingsForm form;
    private Project project;
    private Timer timer;
    private DesgenProjectSettings settings;
    private ImageIcon icon;
    private ImageIcon toolBarIcon;

    public ProjectSettingsComponent(Project project) throws Exception
    {
        this.project = project;
        settings = new DesgenProjectSettings();

        Image logo = ImageIO.read(this.getClass().getResourceAsStream("/org/techdesgen/images/desgen_logo.gif"));
        icon = new ImageIcon(logo);
        logo = ImageIO.read(this.getClass().getResourceAsStream("/org/techdesgen/images/desgen_logo_small.png"));
        toolBarIcon = new ImageIcon(logo);
    }

    public void initComponent()
    {
    }

    public void disposeComponent()
    {
    }

    @NotNull
    public String getComponentName()
    {
        return "TechDesGenProjectSettingsComponent";
    }

    @Nls
    public String getDisplayName()
    {
        return "Tech Design Generator";
    }

    public Icon getIcon()
    {
        return icon;
    }

    public String getHelpTopic()
    {
        return null;
    }

    public JComponent createComponent()
    {
        if (form == null)
        {
            form = new DesgenProjectSettingsForm();
        }
        return form.getRootPanel();
    }

    public boolean isModified()
    {
        return form != null && form.isModified(settings);
    }

    public void apply() throws ConfigurationException
    {
        if (form != null)
        {
            // Get data from form to component
            form.getData(settings);
            applyGlobalSettings();
        }
    }


    public void reset()
    {
        if (form != null)
        {
            // Reset form data from component
            form.setData(settings);
        }
    }

    public void disposeUIResources()
    {
        form = null;
    }

    public void projectOpened()
    {
        /*
        applyGlobalSettings();
        IdeaUtil.PROJECT = project;
        ContentFactory contentFactory = PeerFactory.getInstance().getContentFactory();
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(this.project);

        ToolWindow toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.LEFT);
        ToolWindow consoleWindow = toolWindowManager.registerToolWindow(CONSOLE_WINDOW_ID, false, ToolWindowAnchor.BOTTOM);

        ChangeSetController changeSetController = new ChangeSetController();
        ConsoleView view = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        consoleWindow.setIcon(toolBarIcon);

        Content content = contentFactory.createContent(changeSetController.getView(), "Changes", false);
        consoleWindow.getContentManager().addContent(content);

        HudsonTestController testController = new HudsonTestController(new ConsoleViewAdapter(view, consoleWindow));

        JSplitPane testSplitPane = new JSplitPane();
        testSplitPane.setDividerLocation(400);
        testSplitPane.setLeftComponent(testController.getView());
        testSplitPane.setRightComponent(view.getComponent());

        content = contentFactory.createContent(testSplitPane, "Tests", false);
        consoleWindow.getContentManager().addContent(content);

        StatusBar sb = WindowManager.getInstance().getStatusBar(this.project);
        statusIndicator = new StatusIndicator(sb);
        sb.addCustomIndicationComponent(statusIndicator);

        statusIndicator.updateTooltip("Not Initialized");

        TaskManager taskManager = new TaskManager();
        taskManager.setCompletionService(new ExecutorCompletionService<ITask>(new ThreadPoolExecutor(10, 10, 300, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>())));
        jobController = new HudsonJobController(this.settings, this.statusIndicator, testController, changeSetController, taskManager);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(jobController.getView(), BorderLayout.CENTER);

        content = contentFactory.createContent(panel, "Hudson", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(toolBarIcon);

        restartTimer();
        */
    }

    private void applyGlobalSettings()
    {
        /*
        IOUtil.setHudsonSettings(settings);
        if (settings.isDebugCheckBox())
        {
            LogUtil.setLoglevel(Level.FINEST);
        if (settings.getTimeOutTextField() == null)
        {
            settings.setTimeOutTextField("30");
        }
        net.whippetcode.hudson.util.IOUtil.setReadTimeout(Integer.parseInt(settings.getTimeOutTextField()) * 1000);
        */
    }

    public void projectClosed()
    {
        StatusBar sb = WindowManager.getInstance().getStatusBar(this.project);
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(this.project);
        toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
        toolWindowManager.unregisterToolWindow(CONSOLE_WINDOW_ID);
    }

    /*
    public DesgenProjectSettings getState()
    {
        return settings;
            */

    @Override
    public DesgenProjectSettings getState() {
        return settings;
    }

    @Override
    public void loadState(DesgenProjectSettings o) {
        XmlSerializerUtil.copyBean(o, settings);
    }
}