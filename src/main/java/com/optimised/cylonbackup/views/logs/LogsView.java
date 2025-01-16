package com.optimised.cylonbackup.views.logs;

import com.optimised.cylonbackup.data.entity.Logs;
import com.optimised.cylonbackup.data.service.LogService;
import com.optimised.cylonbackup.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Logs")
@Route(value = "logs", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})

public class LogsView extends VerticalLayout {

    final LogService logService;

        Grid<Logs> grid= new Grid<>(Logs.class);
        TextField filterByLevel = new TextField();
        TextField filterByMessage = new TextField();

        LogsView(LogService logService){
            this.logService = logService;
            addClassName("list-view");
            setSizeFull();
            configureGrid();
            add(getToolbar(), getContent());
            updateList();
        }

        public void configureGrid(){
            grid.addClassNames("site-grid");
            grid.setSizeFull();
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            grid.setColumns( "logDate","level","message");
            grid.getColumns().forEach(col -> col.setAutoWidth(true));
        }

        private HorizontalLayout getToolbar() {
            filterByLevel.setPlaceholder("Filter by level...");
          filterByLevel.setClearButtonVisible(true);
          filterByLevel.setValueChangeMode(ValueChangeMode.LAZY);
          filterByLevel.addValueChangeListener(e -> updateList());
            filterByMessage.setPlaceholder("Filter by message...");
          filterByMessage.setClearButtonVisible(true);
          filterByMessage.setValueChangeMode(ValueChangeMode.LAZY);
          filterByMessage.addValueChangeListener(e -> updateList());

            var toolbar = new HorizontalLayout(filterByLevel,filterByMessage);
            toolbar.addClassName("toolbar");
            return toolbar;
        }

        private Component getContent() {
            HorizontalLayout content = new HorizontalLayout(grid);
            content.addClassNames("content");
            content.setSizeFull();
            return content;
        }

        private void updateList() {
          grid.setItems(logService.findByLevelAndMessage(filterByLevel.getValue(), filterByMessage.getValue()));
        }
}
