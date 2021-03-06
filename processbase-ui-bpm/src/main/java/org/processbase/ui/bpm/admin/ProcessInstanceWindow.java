/**
 * Copyright (C) 2011 PROCESSBASE Ltd.
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.processbase.ui.bpm.admin;

import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import org.ow2.bonita.light.LightProcessInstance;
import org.processbase.ui.core.ProcessbaseApplication;
import org.processbase.ui.core.template.ButtonBar;
import org.processbase.ui.core.template.ByteArraySource;
import org.processbase.ui.core.template.ConfirmDialog;
import org.processbase.ui.core.template.PbWindow;

/**
 *
 * @author mgubaidullin
 */
public class ProcessInstanceWindow extends PbWindow implements Button.ClickListener {

    private LightProcessInstance process;
    private ButtonBar buttons = new ButtonBar();
    private Button closeBtn;
    private Button refreshBtn;
    private Button deleteBtn;
    private Button cancelBtn;
    private ActivitiesPanel activitiesPanel;
    private Embedded processImage = null;
    private VerticalSplitPanel layout = new VerticalSplitPanel();
    private VerticalLayout imageLayout = new VerticalLayout();
    private boolean managed = false;

    public ProcessInstanceWindow(LightProcessInstance process, boolean managed) {
        super(null);
        this.process = process;
        this.managed = managed;
    }

    public void initUI() {
        try {
            setContent(layout);
            ByteArraySource bas = new ByteArraySource(ProcessbaseApplication.getCurrent().getBpmModule().getProcessDiagramm(process));
            StreamResource imageResource = new StreamResource(bas, "processInstance.png", this.getApplication());
            imageResource.setCacheTime(1000);
            processImage = new Embedded("", imageResource);
//            imageLayout.setWidth("100%");
//            imageLayout.setHeight("500px");
            imageLayout.setSizeUndefined();
            imageLayout.addComponent(processImage);
            imageLayout.setStyleName(Reindeer.LAYOUT_WHITE);
            imageLayout.setMargin(false);
            imageLayout.setSpacing(false);

            closeBtn = new Button(ProcessbaseApplication.getCurrent().getPbMessages().getString("btnClose"), this);
            refreshBtn = new Button(ProcessbaseApplication.getCurrent().getPbMessages().getString("btnRefresh"), this);
            deleteBtn = new Button(ProcessbaseApplication.getCurrent().getPbMessages().getString("btnDelete"), this);
            cancelBtn = new Button(ProcessbaseApplication.getCurrent().getPbMessages().getString("btnCancel"), this);

            buttons.addButton(deleteBtn);
            buttons.setComponentAlignment(deleteBtn, Alignment.MIDDLE_LEFT);
            buttons.addButton(cancelBtn);
            buttons.setComponentAlignment(cancelBtn, Alignment.MIDDLE_LEFT);
            buttons.addButton(refreshBtn);
            buttons.setComponentAlignment(refreshBtn, Alignment.MIDDLE_RIGHT);
            buttons.setExpandRatio(refreshBtn, 1);
            buttons.addButton(closeBtn);
            buttons.setComponentAlignment(closeBtn, Alignment.MIDDLE_RIGHT);
            buttons.setMargin(true, true, true, true);
            buttons.setWidth("100%");

            layout.setMargin(false);
            layout.setSizeFull();
//            Float f = layout.getHeight() - activitiesPanel.getHeight();
            layout.setSplitPosition(400, VerticalSplitPanel.UNITS_PIXELS);
            layout.setLocked(true);
            layout.setStyleName(Reindeer.SPLITPANEL_SMALL);
            layout.addComponent(imageLayout);

            activitiesPanel = new ActivitiesPanel(process.getProcessInstanceUUID());
            layout.addComponent(activitiesPanel);
            activitiesPanel.initUI();
            activitiesPanel.refreshTable();

            if (managed) {
                activitiesPanel.addComponent(buttons);
            }

            String pdUUID = process.getProcessDefinitionUUID().toString();
            setCaption(ProcessbaseApplication.getCurrent().getPbMessages().getString("ProcessActivities")
                    + " \"" + pdUUID.split("--")[0] + " " + pdUUID.split("--")[1] + " \"");
            setWidth("90%");
            setHeight("95%");
            setModal(true);
            setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(ex.getMessage());
        }
    }

    public void buttonClick(ClickEvent event) {
        try {
            if (event.getButton().equals(refreshBtn)) {
                activitiesPanel.refreshTable();
            } else if (event.getButton().equals(deleteBtn)) {
                delete();
            } else if (event.getButton().equals(cancelBtn)) {
                cancel();
            } else if (event.getButton().equals(closeBtn)) {
                close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(ex.getMessage());
        }
    }

    private void delete() {
        ConfirmDialog.show(getApplication().getMainWindow(),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("windowCaptionConfirm"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("questionDeleteProcessInstance"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("btnYes"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("btnNo"),
                new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            try {
                                ProcessbaseApplication.getCurrent().getBpmModule().deleteProcessInstance(process.getProcessInstanceUUID());
                                showInformation(ProcessbaseApplication.getCurrent().getPbMessages().getString("executedSuccessfully"));
                            } catch (Exception ex) {
                                showError(ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void cancel() {
        ConfirmDialog.show(getApplication().getMainWindow(),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("windowCaptionConfirm"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("questionCancelProcessInstance"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("btnYes"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("btnNo"),
                new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            try {
                                ProcessbaseApplication.getCurrent().getBpmModule().cancelProcessInstance(process.getProcessInstanceUUID());
                                showInformation(ProcessbaseApplication.getCurrent().getPbMessages().getString("executedSuccessfully"));
                            } catch (Exception ex) {
                                showError(ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }
}
