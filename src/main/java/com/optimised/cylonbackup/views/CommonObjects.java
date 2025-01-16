package com.optimised.cylonbackup.views;

import com.optimised.cylonbackup.data.entity.Site;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class CommonObjects {

    public static void setSiteGrid(Grid<Site> grid) {
        grid.addClassNames("site-grid");
        grid.setSizeFull();
        grid.setColumns("storeNumber", "name", "siteNumber", "directory", "telephone", "ipAddr", "port");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    public static void setFilters(TextField filterByName, IntegerField filterByStoreNo) {
        filterByName.setPlaceholder("Filter by name...");
        filterByName.setClearButtonVisible(true);
        filterByName.setValueChangeMode(ValueChangeMode.LAZY);
        filterByStoreNo.setPlaceholder("Filter by store no...");
        filterByStoreNo.setClearButtonVisible(true);
        filterByStoreNo.setValueChangeMode(ValueChangeMode.LAZY);
    }

    public static void setButtonsSaveDeleteCancel(Button save, Button cancel, Button delete) {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        delete.addClickShortcut(Key.DELETE);
        cancel.addClickShortcut(Key.ESCAPE);
    }

    public static void setButtonsSaveCancel(Button save, Button cancel) {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);
    }
}
