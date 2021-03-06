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
package org.processbase.ui.bpm.generator;

import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.AttachmentInstance;
import org.ow2.bonita.facade.runtime.Document;
import org.ow2.bonita.facade.runtime.InitialAttachment;
import org.ow2.bonita.facade.runtime.impl.DocumentImpl;
import org.ow2.bonita.facade.runtime.impl.InitialAttachmentImpl;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.search.DocumentResult;
import org.ow2.bonita.search.DocumentSearchBuilder;
import org.ow2.bonita.search.index.DocumentIndex;
import org.ow2.bonita.util.GroovyExpression;
import org.processbase.ui.core.ProcessbaseApplication;
import org.processbase.ui.core.bonita.forms.ActionType;
import org.processbase.ui.core.bonita.forms.Actions.Action;
import org.processbase.ui.core.bonita.forms.Activities;
import org.processbase.ui.core.bonita.forms.FormsDefinition;
import org.processbase.ui.core.bonita.forms.PageFlow;
import org.processbase.ui.core.bonita.forms.PageFlow.Pages.Page;
import org.processbase.ui.core.bonita.forms.SelectMode;
import org.processbase.ui.core.bonita.forms.ValuesList;
import org.processbase.ui.core.bonita.forms.VariableType;
import org.processbase.ui.core.bonita.forms.Widget;
import org.processbase.ui.core.bonita.forms.WidgetGroup;
import org.processbase.ui.core.bonita.forms.WidgetType;
import org.processbase.ui.core.template.HumanTaskWindow;
import org.processbase.ui.core.template.ImmediateUpload;

/**
 *
 * @author marat
 */
public class GeneratedWindow extends HumanTaskWindow implements Button.ClickListener {

    private BarResource barResource;
    private FormsDefinition formsDefinition;
    private PageFlow pageFlow;
    private HashMap<Component, Widget> components = new HashMap<Component, Widget>();
    private HashMap<String, Component> fields = new HashMap<String, Component>();
    private ArrayList<GridLayout> pages = new ArrayList<GridLayout>();
    private int currentPage = 0;
    private List<AttachmentInstance> attachmentInstances = null;
    private ArrayList<InitialAttachment> initialAttachments = new ArrayList<InitialAttachment>();
    private Map<Document, byte[]> attachmentBodies = new HashMap<Document, byte[]>();
    private Map<String, Document> attachmentsDefinitions = new HashMap<String, Document>();
    private Map<String, Object> groovyScripts = new HashMap<String, Object>();

    public GeneratedWindow(String caption) {
        super(caption);
    }

    @Override
    public void initUI() {
        super.initUI();
        try {
            if (taskInstance != null && !taskInstance.getState().equals(ActivityState.FINISHED) && !taskInstance.getState().equals(ActivityState.ABORTED) && !taskInstance.getState().equals(ActivityState.CANCELLED)) {
                pageFlow = getPageFlow(taskInstance.getActivityName());
            } else if (taskInstance == null) {
                pageFlow = getPageFlow();
            }
            if (pageFlow != null && pageFlow.getPages() != null && !pageFlow.getPages().getPages().isEmpty()) {
                generateWindow();
            } else {
                showError(ProcessbaseApplication.getCurrent().getPbMessages().getString("ERROR_UI_NOT_DEFINED"));
//                close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(ex.getMessage());
        }
    }

    protected void generateWindow() throws Exception {
        prepareAttachmentDefinitions();
        prepareGroovyScripts();
        for (Page page : pageFlow.getPages().getPages()) {
            TableStyle ts = barResource.getTableStyle(page);
            GridLayout gridLayout = new GridLayout(ts.getColumns(), ts.getRows());
            gridLayout.setMargin(false, true, true, true);
            gridLayout.setSpacing(true);
            for (Object wg : page.getWidgets().getWidgetsAndGroups()) {
                Component component = null;
                if (wg instanceof Widget) {
                    Widget widget = (Widget) wg;
                    component = getComponent(widget);
                    components.put(component, widget);
                    fields.put("field_" + widget.getId(), component);
                    ComponentStyle componentStyle = ts.getElements().get(widget.getId());
                    int fColumn = componentStyle.getPosition().getFColumn();
                    int fRow = componentStyle.getPosition().getFRow();
                    int tColumn = componentStyle.getPosition().getTColumn();
                    int tRow = componentStyle.getPosition().getTRow();
//                    System.out.println(widget.getId() + " " + fColumn + " " + tColumn + " " + fRow + " " + tRow);
                    CSSProperty cssProperty = componentStyle.getCss();
                    if (cssProperty != null) {
//                        System.out.print(widget.getId() + " H:" + cssProperty.getHeigth());
//                        System.out.print(" W:" + cssProperty.getWidth());
//                        System.out.println(" A:" + cssProperty.getAlign());
                    } else {
                        if (!(component instanceof Table) && !(component instanceof Button) && (fColumn == tColumn)) {
                            component.setWidth("200px");
                        }
                    }
                    gridLayout.addComponent(component, fColumn, fRow, tColumn, tRow);
                } else if (wg instanceof WidgetGroup) {
                }
            }
            pages.add(gridLayout);
        }
        taskPanel.setContent(pages.get(currentPage));
        taskPanel.setCaption(pageFlow.getPages().getPages().get(currentPage).getPageLabel());
    }

    private Component getComponent(Widget widget) {
        Component component = null;
        Object value = null;
        DataFieldDefinition dfd = null;
        Collection options = null;
        try {
            if (widget.getInitialValue() != null && widget.getInitialValue().getExpression() != null) {
                value = groovyScripts.get(widget.getInitialValue().getExpression());
            }
            if (widget.getAvailableValues() != null) {
                if (widget.getAvailableValues().getExpression() != null) {
                    options = (Collection) groovyScripts.get(widget.getAvailableValues().getExpression());
                } else if (!widget.getAvailableValues().getValuesList().getAvailableValues().isEmpty()) {
                    options = new ArrayList<String>();
                    for (ValuesList.AvailableValue avalue : widget.getAvailableValues().getValuesList().getAvailableValues()) {
                        options.add(avalue.getValue());
                    }
                }
            }

            if (widget.getType().equals(WidgetType.TEXTBOX)) {
                component = getTextField(widget);
            }
            if (widget.getType().equals(WidgetType.DATE)) {
                component = getPopupDateField(widget);
            }
            if (widget.getType().equals(WidgetType.TEXTAREA)) {
                component = getTextArea(widget);
            }
            if (widget.getType().equals(WidgetType.RICH_TEXTAREA)) {
                component = getRichTextArea(widget);
            }
            if (widget.getType().equals(WidgetType.TEXT)) {
                component = getTextField(widget);
            }
            if (widget.getType().equals(WidgetType.PASSWORD)) {
                component = getPasswordField(widget);
            }
            if (widget.getType().equals(WidgetType.MESSAGE)) {
                component = getLabel(widget, value);
            }
            if (widget.getType().equals(WidgetType.LISTBOX_SIMPLE)) {
                component = getNativeSelect(widget, options);
            }
            if (widget.getType().equals(WidgetType.SUGGESTBOX)) {
                component = getComboBox(widget, options);
            }
            if (widget.getType().equals(WidgetType.RADIOBUTTON_GROUP)) {
                component = getOptionGroup(widget, options);
            }
            if (widget.getType().equals(WidgetType.LISTBOX_MULTIPLE)) {
                component = getListSelect(widget, options);
            }
            if (widget.getType().equals(WidgetType.CHECKBOX)) {
                component = getCheckBox(widget);
            }
            if (widget.getType().equals(WidgetType.EDITABLE_GRID)) {
                component = new GeneratedTable(widget, value, null, groovyScripts);
            }
            if (widget.getType().equals(WidgetType.TABLE)) {
                component = new GeneratedTable(widget, options, value, groovyScripts);
            }
            if (widget.getType().equals(WidgetType.CHECKBOX_GROUP)) {
                component = getOptionGroup(widget, options);
            }
            if (widget.getType().equals(WidgetType.FILEUPLOAD)) {
                component = getUpload(widget);
            }
            if (widget.getType().equals(WidgetType.BUTTON_SUBMIT)
                    || widget.getType().equals(WidgetType.BUTTON_NEXT)
                    || widget.getType().equals(WidgetType.BUTTON_PREVIOUS)) {
                component = getButton(widget);
            }

            component = (component != null) ? component : new Label("");
            // add general atrubutes
            // setWidth
//            if (!(component instanceof Button)) {
//                component.setWidth((widget.getInputWidth() != null) ? widget.getInputWidth() : "200px");
//            } else if ((component instanceof Button) && widget.getInputWidth() != null) {
//                component.setWidth(widget.getInputWidth());
//            }
            // setHeght
//            if (widget.getInputHeight() != null) {
//                component.setHeight(widget.getInputHeight());
//            }

            if (component instanceof AbstractField) {
                if (value != null) {
                    ((AbstractField) component).setValue(value);
                }
//                System.out.println(widget.getLabel() + " value = " + widget.isMandatory());
                if (widget.isMandatory() != null) {
                    ((AbstractField) component).setRequired(widget.isMandatory());
                }
                ((AbstractField) component).setRequiredError(widget.getLabel() + ProcessbaseApplication.getCurrent().getPbMessages().getString("fieldRequired"));
                ((AbstractField) component).setDescription(widget.getTitle() != null ? widget.getTitle() : "");
                ((AbstractField) component).setInvalidCommitted(false);
                ((AbstractField) component).setWriteThrough(false);
            }
            if (widget.isReadonly() != null) {
                component.setReadOnly(widget.isReadonly());
            }
            return component;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Label("");
    }

    private TextField getTextField(Widget widget) {
        TextField component = new TextField(widget.getLabel());
        if (widget.getValidators() != null) {
            component.addValidator(new GeneratedValidator(widget, taskInstance, processDefinition, getApplication().getLocale(), ProcessbaseApplication.getCurrent().getBpmModule()));
        }
//else if (dfd != null && dfd.getDataTypeClassName().equals("java.lang.Double")) {
//            component.addValidator(
//                    new DoubleValidator((widget.getLabel() != null ? widget.getLabel() : widget.getName()) + " "
//                    + ProcessbaseApplication.getCurrent().getMessages().getString("validatorDoubleError")));
//        } else if (dfd != null && dfd.getDataTypeClassName().equals("java.lang.Long")) {
//            component.addValidator(
//                    new LongValidator((widget.getLabel() != null ? widget.getLabel() : widget.getName()) + " "
//                    + ProcessbaseApplication.getCurrent().getMessages().getString("validatorIntegerError")));
//        }
        component.setNullRepresentation("");
        return component;
    }

    private TextArea getTextArea(Widget widget) {
        TextArea component = new TextArea(widget.getLabel());
//        if (widget.getValidatorName() != null) {
//            component.addValidator(new GeneratedValidator(widget, taskInstance, processDefinition, getApplication().getLocale(), ProcessbaseApplication.getCurrent().getBpmModule()));
//        }
        component.setNullRepresentation("");
        return component;
    }

    private TextField getPasswordField(Widget widget) {
        TextField component = new TextField(widget.getLabel());
        component.setNullRepresentation("");
        return component;
    }

    private Label getLabel(Widget widget, Object value) {
        Label component = new Label(value.toString());
        component.setContentMode(Label.CONTENT_XHTML);
        return component;
    }

    private PopupDateField getPopupDateField(Widget widget) {
        PopupDateField component = new PopupDateField(widget.getLabel());
        component.setResolution(PopupDateField.RESOLUTION_DAY);
        return component;
    }

    private RichTextArea getRichTextArea(Widget widget) {
        RichTextArea component = new RichTextArea(widget.getLabel());
        component.setNullRepresentation("");
        return component;
    }

    private NativeSelect getNativeSelect(Widget widget, Collection options) {
        NativeSelect component = new NativeSelect(widget.getLabel(), options);
        return component;
    }

    private ComboBox getComboBox(Widget widget, Collection options) {
        ComboBox component = new ComboBox(widget.getLabel(), options);
        component.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
        return component;
    }

    private OptionGroup getOptionGroup(Widget widget, Collection options) {
        OptionGroup component = new OptionGroup(widget.getLabel(), options);
        if (widget.getSelectMode() != null && widget.getSelectMode().equals(SelectMode.MULTIPLE)) {
            component.setMultiSelect(true);
        }
        return component;
    }

    private ListSelect getListSelect(Widget widget, Collection options) {
        ListSelect component = new ListSelect(widget.getLabel(), options);
        component.setMultiSelect(true);
        return component;
    }

    private CheckBox getCheckBox(Widget widget) {
        CheckBox component = new CheckBox(widget.getLabel());
        return component;
    }

    private Table getTable(Widget widget, Collection options) {
        Table component = new Table(widget.getLabel());
        component.setMultiSelect(true);
        return component;
    }

    private ImmediateUpload getUpload(Widget widget) {
        System.out.println("widget.getInitialValue().getExpression() = " + widget.getInitialValue().getExpression());
        ImmediateUpload component = new ImmediateUpload(widget, attachmentsDefinitions.get(widget.getInitialValue().getExpression()), ProcessbaseApplication.getCurrent().getPbMessages());
        return component;
    }

    private Button getButton(Widget widget) {
        Button component = new Button(widget.getLabel());
        component.addListener((Button.ClickListener) this);
        if (widget.isLabelButton()) {
            component.setStyleName(Reindeer.BUTTON_LINK);
        }
        return component;
    }

    @Override
    public void buttonClick(ClickEvent event) {
        super.buttonClick(event);
        try {
            if (components.containsKey(event.getButton())) {
                Button btn = event.getButton();
                if (getWidgets(btn).getType().equals(WidgetType.BUTTON_SUBMIT)) {
                    commit();
                    setProcessVariables();
                    if (taskInstance == null) {
                        prepareAttachmentsToSave(true);
                        ProcessInstanceUUID piUUID = ProcessbaseApplication.getCurrent().getBpmModule().startNewProcess(processDefinition.getUUID(), processInstanceVariables, initialAttachments);
                    } else {
                        prepareAttachmentsToSave(false);
                        ProcessbaseApplication.getCurrent().getBpmModule().finishTask(taskInstance, true, processInstanceVariables, activityInstanceVariables, attachmentBodies);
                    }
                    close();

                } else if (getWidgets(btn).getType().equals(WidgetType.BUTTON_NEXT)) {
                    commitPage(pages.get(currentPage));
                    currentPage = (pages.size() > (currentPage + 1)) ? currentPage + 1 : currentPage;
                    taskPanel.setContent(pages.get(currentPage));
                    taskPanel.setCaption(pageFlow.getPages().getPages().get(currentPage).getPageLabel());
                } else if (getWidgets(btn).getType().equals(WidgetType.BUTTON_PREVIOUS)) {
                    commitPage(pages.get(currentPage));
                    currentPage = (currentPage != 0) ? currentPage - 1 : currentPage;
                    taskPanel.setContent(pages.get(currentPage));
                    taskPanel.setCaption(pageFlow.getPages().getPages().get(currentPage).getPageLabel());
                }
            }
        } catch (InvalidValueException ex) {
            ex.printStackTrace();
            showMessage(ex.getMessage(), Notification.TYPE_ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage(ex.getMessage(), Notification.TYPE_ERROR_MESSAGE);
        }
    }

    private void commit() {
        for (GridLayout grid : pages) {
            commitPage(grid);
        }
    }

    private void commitPage(GridLayout page) {
        for (Iterator<Component> iterator = page.getComponentIterator(); iterator.hasNext();) {
            Component comp = iterator.next();
            if (comp instanceof AbstractField) {
                try {
                    ((AbstractField) comp).setComponentError(null);
                    ((AbstractField) comp).validate();
                } catch (InvalidValueException ex) {
                    if (ex instanceof EmptyValueException) {
                        ((AbstractField) comp).setComponentError(new UserError(((AbstractField) comp).getRequiredError()));
                    }
                    throw ex;
                }

            }
        }
    }

    private void setProcessVariables() throws Exception {
        Map<String, Object> piVariablesTemp = new HashMap<String, Object>();
        Map<String, Object> aiVariablesTemp = new HashMap<String, Object>();
        for (Page page : pageFlow.getPages().getPages()) {
            if (page.getActions() != null) {
                for (Action action : page.getActions().getActions()) {
                    if (action.getType().equals(ActionType.SET_VARIABLE)) {
                        Component comp = null;
                        Object value = null;
                        if (action.getExpression().startsWith("field")) {
                            comp = fields.get(action.getExpression());
                            if (comp instanceof AbstractField) {
                                value = ((AbstractField) comp).getValue();
                            }
                            if (comp instanceof GeneratedTable) {
                                value = ((GeneratedTable) comp).getTableValue();
                            }
                            if (comp instanceof CheckBox) {
                                value = ((CheckBox) comp).booleanValue();
                            }
                        } else {
                            value = action.getExpression();
                        }
                        if (action.getVariableType().equals(VariableType.PROCESS_VARIABLE)) {
                            piVariablesTemp.put(action.getVariable(), value);
                        } else if (action.getVariableType().equals(VariableType.ACTIVITY_VARIABLE)) {
                            aiVariablesTemp.put(action.getVariable(), value);
                        }
                    }
                }
            }
        }
        processInstanceVariables = piVariablesTemp;
        activityInstanceVariables = aiVariablesTemp;
    }

    private void prepareAttachmentsToSave(boolean initial) {
        try {
            for (Component comp : components.keySet()) {
                Widget widget = components.get(comp);
                if (widget.getVariableBound() != null && widget.getType().equals(WidgetType.FILEUPLOAD) && ((ImmediateUpload) comp).isContentNew()) {
                    ImmediateUpload ui = (ImmediateUpload) comp;
                    System.out.println("widget.getVariableBound() = " + widget.getVariableBound() + " " + ui.getFileName() + " " + ui.getFileBody().length);
                    if (initial) {
                        InitialAttachmentImpl ai = new InitialAttachmentImpl(widget.getVariableBound(), ui.getFileBody());
                        ai.setFileName(ui.getFileName());
                        initialAttachments.add(ai);
                    } else {
                        Document doc = new DocumentImpl(ui.getDocument().getUUID(), ui.getDocument().getName(), ui.getDocument().getProcessDefinitionUUID(),
                                ui.getDocument().getProcessInstanceUUID(), "author", new Date(), "lastmodifyBy", new Date(), true, true, "ver label", "ver series",
                                ui.getFileName(), "mimetype", ui.getFileBody().length);
                        attachmentBodies.put(doc, ui.getFileBody());

                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GeneratedWindow.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
    }

    private void prepareAttachmentDefinitions() {
        try {
            DocumentSearchBuilder builder = new DocumentSearchBuilder();
            builder.criterion(DocumentIndex.PROCESS_DEFINITION_UUID).equalsTo(processDefinition.getUUID().toString());
            if (taskInstance != null) {
                builder.and().criterion(DocumentIndex.PROCESS_INSTANCE_UUID).equalsTo(taskInstance.getProcessInstanceUUID().toString());
            }
            DocumentResult dr = ProcessbaseApplication.getCurrent().getBpmModule().searchDocuments(builder, 1, 999999);
            for (Document document : dr.getDocuments()) {
                System.out.println(" attachmentsDefinitions + " + document.getName());
                attachmentsDefinitions.put(document.getName(), document);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Widget getWidgets(Component c) {
        for (Component comp : components.keySet()) {
            if (comp.equals(c)) {
                return components.get(comp);
            }
        }
        return null;
    }

    private void prepareGroovyScripts() throws Exception {
        HashSet<String> expressions = new HashSet<String>();
        HashMap<String, String> scripts = new HashMap<String, String>();
        HashMap<String, String> strings = new HashMap<String, String>();
        for (Page page : pageFlow.getPages().getPages()) {
            for (Object wg : page.getWidgets().getWidgetsAndGroups()) {
                if (wg instanceof Widget) {
                    Widget widget = (Widget) wg;
                    if (widget.getInitialValue() != null && widget.getInitialValue().getExpression() != null) {
                        expressions.add(widget.getInitialValue().getExpression());
                    }
                    if (widget.getAvailableValues() != null && widget.getAvailableValues().getExpression() != null) {
                        expressions.add(widget.getAvailableValues().getExpression());
                    }
                    if (widget.getHorizontalHeader() != null) {
                        expressions.add(widget.getHorizontalHeader());
                    }
                } else if (wg instanceof WidgetGroup) {
                    // not yet implemented
                }
            }
        }
        for (String expression : expressions) {
//            System.out.println("expression = " + expression);
            if (expression != null && !expression.isEmpty()) {
                int begin = expression.indexOf(GroovyExpression.START_DELIMITER);
                int end = expression.indexOf(GroovyExpression.END_DELIMITER);
                if (begin >= end) {
                    strings.put(expression, expression);
                } else {
                    scripts.put(expression, expression);
                }
            }
        }
        if (taskInstance != null && !scripts.isEmpty()) {
            groovyScripts = ProcessbaseApplication.getCurrent().getBpmModule().evaluateGroovyExpressions(scripts, taskInstance.getUUID(), null, false, false);
        } else if (taskInstance == null && !scripts.isEmpty()) {
            groovyScripts = ProcessbaseApplication.getCurrent().getBpmModule().evaluateGroovyExpressions(scripts, processDefinition.getUUID(), null);
        }
        for (String string : strings.keySet()) {
            groovyScripts.put(string, strings.get(string));
        }
//
//        for (String key : groovyScripts.keySet()){
//            System.out.println(key + " = " + groovyScripts.get(key) + " " + groovyScripts.get(key).getClass().getCanonicalName());
//        }
    }

    private String getPureScript(String script) {
        script = script.replace(GroovyExpression.START_DELIMITER, "");
        int end = script.indexOf(GroovyExpression.END_DELIMITER);
        return script.substring(0, end > 0 ? end : script.length());
    }

    public void setBarResource(BarResource barResource) {
        this.barResource = barResource;
        formsDefinition = this.barResource.getFormsDefinition();
    }

    private PageFlow getPageFlow() {
        return formsDefinition.getProcesses().get(0).getPageflow();
    }

    private PageFlow getPageFlow(String activityName) {
        for (Activities.Activity a : formsDefinition.getProcesses().get(0).getActivities().getActivities()) {
            if (a.getName().equals(activityName)) {
                return a.getPageflow();
            }
        }
        return null;
    }
}
