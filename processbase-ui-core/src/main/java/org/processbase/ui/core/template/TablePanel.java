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
package org.processbase.ui.core.template;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 *
 * @author mgubaidullin
 */
public class TablePanel extends WorkPanel implements Button.ClickListener, Window.CloseListener {

    protected Table table = new Table();
    public int rowCount = 0;

    public TablePanel() {
        super();
    }

    @Override
    public void initUI() {
        super.initUI();
        table.setSizeFull();
        if (getPbWindow().getContent() instanceof PbPanel && ((PbPanel) getPbWindow().getContent()).isControl()) {
            table.setPageLength(20);
        } else {
            table.setPageLength(15);
        }
        table.addStyleName("striped");
//        table.setSelectable(true);
//        table.setMultiSelect(false);
//        table.setImmediate(false);
        horizontalLayout.addComponent(table, 0);
        horizontalLayout.setComponentAlignment(table, Alignment.TOP_LEFT);
        horizontalLayout.setExpandRatio(table, 1);
    }

    public void refreshTable() {
    }

    public TableLinkButton getExecBtn(String description, String iconName, Object t, String action) {
        return new TableLinkButton(description, iconName, t, this, action);
    }

    @Override
    public void buttonClick(ClickEvent event) {
    }

    @Override
    public void windowClose(CloseEvent e) {
        refreshTable();
    }

    public Object getTableValue() {
        return table.getValue();
    }

    public Table getTable() {
        return table;
    }
}
