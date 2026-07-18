package com.example.admin.ui;

import com.example.admin.security.AuthService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("登录 - vaadin-admin")
public class LoginView extends VerticalLayout {

    public LoginView(AuthService authService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("vaadin-admin");
        TextField username = new TextField("用户名");
        PasswordField password = new PasswordField("密码");
        Button login = new Button("登 录");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        username.setWidthFull();
        password.setWidthFull();
        login.setWidthFull();

        VerticalLayout card = new VerticalLayout(title, username, password, login);
        card.setWidth("360px");
        card.setAlignItems(Alignment.CENTER);
        card.setPadding(true);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");
        add(card);

        login.addClickShortcut(Key.ENTER);
        login.addClickListener(e -> {
            try {
                authService.login(username.getValue(), password.getValue());
                login.getUI().ifPresent(ui -> ui.navigate(HomeView.class));
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
    }
}
