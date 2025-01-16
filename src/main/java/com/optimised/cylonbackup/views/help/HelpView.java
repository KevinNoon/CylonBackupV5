package com.optimised.cylonbackup.views.help;

import com.optimised.cylonbackup.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;


@PageTitle("Help")
@Route(value = "help", layout = MainLayout.class)
@AnonymousAllowed
public class HelpView extends VerticalLayout {

    public HelpView() {

            // Button to open the help file
            Button help= new Button("Open Help File");
            help.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            help.addClickListener(event -> {
                getUI().ifPresent(ui ->
                    ui.getPage().open("help/Cylon Backup Help.pdf", "_blank")
                );
            });
            add(help);
    }
}
