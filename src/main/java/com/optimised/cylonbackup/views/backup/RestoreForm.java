package com.optimised.cylonbackup.views.backup;

import com.optimised.cylonbackup.tools.BackupTools;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.util.List;

public class RestoreForm extends FormLayout {

    final BackupTools backupTools;
    ComboBox<String> backups = new ComboBox<>("Backups");
    Button restore = new Button("Restore");
    Button cancel = new Button("Cancel");

    public RestoreForm(BackupTools backupTools) {
        this.backupTools = backupTools;
        addClassName("path-form");
        add(
            backups,
            createButtonsLayout());
    }

    public void setBackups(List<String> siteBackups){
        if (siteBackups != null && !siteBackups.isEmpty()) {
            backups.setItemLabelGenerator( n -> n.substring(n.length() - 19, n.length() -4));
            backups.setItems(siteBackups);
            backups.setValue(siteBackups.get(0));
        }
    }

    private HorizontalLayout createButtonsLayout() {
        restore.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        restore.addClickShortcut(Key.ENTER);
        restore.addClickListener(v -> {
            backupTools.RestoreSites(List.of(backups.getValue()),false);
        });

        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClickShortcut(Key.ESCAPE);
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));
        return new HorizontalLayout(restore, cancel);
    }

    @Getter
    public static abstract class restoreFormEvent extends ComponentEvent<RestoreForm> {
        private final List<String> backups;

        protected restoreFormEvent(RestoreForm source, List<String> backups) {
            super(source, false);
            this.backups = backups;
        }

    }

    public static class SaveEvent extends restoreFormEvent {
        SaveEvent(RestoreForm source, List<String> backups) {
            super(source, backups);
        }
    }

    public static class DeleteEvent extends restoreFormEvent {
        DeleteEvent(RestoreForm source, List<String> backups) {
            super(source, backups);
        }

    }

    public static class CloseEvent extends restoreFormEvent {
        CloseEvent(RestoreForm source) {
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
