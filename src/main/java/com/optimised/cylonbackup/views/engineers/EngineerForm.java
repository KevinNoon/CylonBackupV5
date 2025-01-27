package com.optimised.cylonbackup.views.engineers;

import com.optimised.cylonbackup.data.entity.Engineer;
import com.optimised.cylonbackup.views.CommonObjects;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

public class EngineerForm extends FormLayout {
    public void setEngineer(Engineer engineer) {
        binder.setBean(engineer);
    }

    TextField fore_name = new TextField("Fore Name");
    TextField last_name = new TextField("Last Name");
    EmailField email = new EmailField("eMail");
    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button cancel = new Button("Cancel");
    Binder<Engineer> binder = new BeanValidationBinder<>(Engineer.class);

    public EngineerForm() {
        addClassName("engineer-form");
        binder.bindInstanceFields(this);
        add(
                fore_name,
                last_name,
                email,
                createButtonsLayout());
    }

    private HorizontalLayout createButtonsLayout() {
        CommonObjects.setButtonsSaveDeleteCancel(save, cancel, delete);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> validateAndDelete());
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, delete, cancel);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }
    public static abstract class EngineerFormEvent extends ComponentEvent<EngineerForm> {
        private Engineer engineer;

        protected EngineerFormEvent(EngineerForm source, Engineer engineer) {
            super(source, false);
            this.engineer = engineer;
        }

        public Engineer getEngineer() {
            return engineer;
        }
    }

    private void validateAndDelete(){
        if (binder.isValid() && binder.getBean().getId() != null) {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm Deletion");
            confirmDialog.setText("Are you sure you want to delete this engineer?");
            confirmDialog.setConfirmButton("Delete", event -> {
                fireEvent(new DeleteEvent(this, binder.getBean()));
            });
            confirmDialog.setCancelButton("Cancel", event -> fireEvent(new CloseEvent(this)));
            confirmDialog.open();
        }
    }


    public static class SaveEvent extends EngineerFormEvent {
        SaveEvent(EngineerForm source, Engineer engineer) {
            super(source, engineer);
        }
    }

    public static class DeleteEvent extends EngineerFormEvent {
        DeleteEvent(EngineerForm source, Engineer engineer) {
            super(source, engineer);
        }

    }

    public static class CloseEvent extends EngineerFormEvent {
        CloseEvent(EngineerForm source) {
            super(source, null);
        }
    }

    public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }
    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }
}
