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
package org.processbase.ui.bpm.identity;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import java.util.List;
import org.ow2.bonita.facade.identity.User;
import org.processbase.ui.core.Constants;
import org.processbase.ui.core.ProcessbaseApplication;
import org.processbase.ui.core.template.ConfirmDialog;
import org.processbase.ui.core.template.TableLinkButton;
import org.processbase.ui.core.template.TablePanel;

/**
 *
 * @author marat gubaidullin
 */
public class UsersPanel extends TablePanel implements
        Button.ClickListener,
        Window.CloseListener {

    public UsersPanel() {
        super();
    }

    @Override
    public void initUI() {
        super.initUI();
        table.addContainerProperty("username", TableLinkButton.class, null, ProcessbaseApplication.getCurrent().getPbMessages().getString("tableCaptionUsername"), null, null);
//        table.setColumnExpandRatio("name", 1);
        table.addContainerProperty("lastname", String.class, null, ProcessbaseApplication.getCurrent().getPbMessages().getString("tableCaptionLastname"), null, null);
        table.addContainerProperty("firstname", String.class, null, ProcessbaseApplication.getCurrent().getPbMessages().getString("tableCaptionFirstname"), null, null);
        table.addContainerProperty("email", String.class, null, ProcessbaseApplication.getCurrent().getPbMessages().getString("tableCaptionEmail"), null, null);
        table.addContainerProperty("actions", TableLinkButton.class, null, ProcessbaseApplication.getCurrent().getPbMessages().getString("tableCaptionActions"), null, null);
        table.setImmediate(true);
    }

    @Override
    public void refreshTable() {
        try {
            table.removeAllItems();
            List<User> users = ProcessbaseApplication.getCurrent().getBpmModule().getAllUsers();

            for (User user : users) {
                Item woItem = table.addItem(user);
                TableLinkButton teb = new TableLinkButton(user.getUsername(), "", null, user, this, Constants.ACTION_OPEN);
                woItem.getItemProperty("username").setValue(teb);
                woItem.getItemProperty("lastname").setValue(user.getLastName());
                woItem.getItemProperty("firstname").setValue(user.getFirstName());
                woItem.getItemProperty("email").setValue(user.getProfessionalContactInfo() != null ? user.getProfessionalContactInfo().getEmail() : "");
                TableLinkButton tlb = new TableLinkButton(ProcessbaseApplication.getCurrent().getPbMessages().getString("btnDelete"), "icons/cancel.png", user, this, Constants.ACTION_DELETE);
                woItem.getItemProperty("actions").setValue(tlb);
            }
            table.setSortContainerPropertyId("username");
            table.setSortAscending(false);
            table.sort();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(ex.getMessage());
        }
    }

    @Override
    public void buttonClick(ClickEvent event) {
        super.buttonClick(event);
        if (event.getButton() instanceof TableLinkButton) {
            TableLinkButton execBtn = (TableLinkButton) event.getButton();
            User user = (User) execBtn.getTableValue();
            if (execBtn.getAction().equals(Constants.ACTION_DELETE)) {
                try {
                    removeUser(user);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError(ex.getMessage());
                }
            } else if (execBtn.getAction().equals(Constants.ACTION_OPEN)) {
                UserWindow nuw = new UserWindow(user);
                nuw.addListener((Window.CloseListener) this);
                getWindow().addWindow(nuw);
                nuw.initUI();
            }
        }
    }

    private void removeUser(final User user) {
        ConfirmDialog.show(getApplication().getMainWindow(),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("windowCaptionConfirm"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("removeUser") + "?",
                ProcessbaseApplication.getCurrent().getPbMessages().getString("btnYes"),
                ProcessbaseApplication.getCurrent().getPbMessages().getString("btnNo"),
                new ConfirmDialog.Listener() {

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            try {
                                ProcessbaseApplication.getCurrent().getBpmModule().removeUserByUUID(user.getUUID());
                                table.removeItem(user);
                            } catch (Exception ex) {
                                showError(ex.getMessage());
                                ex.printStackTrace();
                            }
                        }
                    }
                });
    }
}
