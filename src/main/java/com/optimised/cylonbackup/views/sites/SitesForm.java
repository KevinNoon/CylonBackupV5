package com.optimised.cylonbackup.views.sites;

import com.optimised.cylonbackup.data.entity.Site;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;

public class SitesForm extends FormLayout {
    public void setSite(Site site) {
        binder.setBean(site);
    }

    IntegerField storeNumber = new IntegerField("Store Number");
    TextField name = new TextField("Name");
    IntegerField siteNumber = new IntegerField("Site Number");
    Button save = new Button("Save");
    Button cancel = new Button("Cancel");
    Button resetReadOnly = new Button("Reset readonly");
    Binder<Site> binder = new BeanValidationBinder<>(Site.class);


    public SitesForm() {
        addClassName("site-form");
        binder.bindInstanceFields(this);
        name.setReadOnly(true);
        siteNumber.setReadOnly(true);
        add(
            storeNumber,
            name,
            siteNumber,
            createButtonsLayout());
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.getAuthenticatedUser(UserDetails.class).ifPresent(user -> {
            boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
            resetReadOnly.setVisible(isAdmin);
        });
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetReadOnly.addThemeVariants(ButtonVariant.LUMO_ERROR);

        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));
        resetReadOnly.addClickListener(event -> resetReadOnlySites());

        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));
        return new HorizontalLayout(save, cancel, resetReadOnly);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    private void resetReadOnlySites() {
        if (binder.isValid()) {
            fireEvent(new ResetReadOnlySitesEvent(this, binder.getBean()));
        }
    }

    @Getter
    public static abstract class SiteFormEvent extends ComponentEvent<SitesForm> {
        private final Site site;

        protected SiteFormEvent(SitesForm source, Site site) {
            super(source, false);
            this.site = site;
        }
    }

    public static class SaveEvent extends SiteFormEvent {
        SaveEvent(SitesForm source, Site site) {
            super(source, site);
        }
    }

    public static class DeleteEvent extends SiteFormEvent {
        DeleteEvent(SitesForm source, Site site) {
            super(source, site);
        }

    }

    public static class CloseEvent extends SiteFormEvent {
        CloseEvent(SitesForm source) {
            super(source, null);
        }
    }

    public static class ResetReadOnlySitesEvent extends SiteFormEvent {
        ResetReadOnlySitesEvent(SitesForm source, Site site) {
            super(source, site);
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

    public Registration addResetReadOnlySitesListener(ComponentEventListener<ResetReadOnlySitesEvent> listener) {
        return addListener(ResetReadOnlySitesEvent.class, listener);
    }
}
