package com.optimised.cylonbackup.views;

import com.optimised.cylonbackup.data.entity.User;
import com.optimised.cylonbackup.data.service.UserService;
import com.optimised.cylonbackup.security.AuthenticatedUser;
import com.optimised.cylonbackup.views.about.AboutView;
import com.optimised.cylonbackup.views.backup.BackupView;
import com.optimised.cylonbackup.views.backup.RestoreView;
import com.optimised.cylonbackup.views.checkout.CheckinView;
import com.optimised.cylonbackup.views.checkout.CheckoutView;
import com.optimised.cylonbackup.views.engineers.EngineersView;
import com.optimised.cylonbackup.views.help.HelpView;
import com.optimised.cylonbackup.views.logs.LogsView;
import com.optimised.cylonbackup.views.settings.EmailSettingsView;
import com.optimised.cylonbackup.views.settings.SettingsView;
import com.optimised.cylonbackup.views.sites.SitesView;
import com.optimised.cylonbackup.views.users.UsersView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;
import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private H2 viewTitle;
    final UserService userService;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(UserService userService, AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    Checkbox isDarkMode = new Checkbox("Dark theme");
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle, isDarkMode);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Cylon Backup Manager");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(AboutView.class)) {
            nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(BackupView.class)) {
            nav.addItem(new SideNavItem("Backup", BackupView.class, LineAwesomeIcon.COLUMNS_SOLID.create()));
        }
        if (accessChecker.hasAccess(RestoreView.class)) {
            nav.addItem(new SideNavItem("Restore", RestoreView.class, LineAwesomeIcon.COLUMNS_SOLID.create()));
        }
        if (accessChecker.hasAccess(SitesView.class)) {
            nav.addItem(new SideNavItem("Sites", SitesView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(CheckoutView.class)) {
            nav.addItem(new SideNavItem("Check out", CheckoutView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(CheckinView.class)) {
            nav.addItem(new SideNavItem("Check in", CheckinView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(EngineersView.class)) {
            nav.addItem(new SideNavItem("Engineers", EngineersView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(UsersView.class)) {
            nav.addItem(new SideNavItem("Users", UsersView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(SettingsView.class)) {
            nav.addItem(new SideNavItem("General Settings", SettingsView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(EmailSettingsView.class)) {
            nav.addItem(new SideNavItem("Email Settings", EmailSettingsView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(LogsView.class)) {
            nav.addItem(new SideNavItem("Logs", LogsView.class, LineAwesomeIcon.FILE.create()));
        }
        if (accessChecker.hasAccess(HelpView.class)) {
            nav.addItem(new SideNavItem("Help", HelpView.class, LineAwesomeIcon.FILE.create()));
        }
        return nav;
    }

    private Footer createFooter() {
        isDarkMode.addValueChangeListener(e -> {
            setTheme(e.getValue());
        });

        Footer layout = new Footer();
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            if (user.getIsdarkmode()!= null) {
                setTheme(user.getIsdarkmode());
                isDarkMode.setValue(user.getIsdarkmode());
            }
            Avatar avatar = new Avatar(user.getName());
            if (user.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
                avatar.setImageResource(resource);}
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                user.setIsdarkmode(isDarkMode.getValue());
                userService.save(user);
                authenticatedUser.logout();
            });
            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);

        return title == null ? "" : title.value();
    }
    private void setTheme(boolean dark) {
        var js = "document.documentElement.setAttribute('theme', $0)";

        getElement().executeJs(js, dark ? Lumo.DARK : Lumo.LIGHT);
    }
}
