package com.example.admin.ui;

import com.example.admin.security.AuthService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.Data;

@Route("login")
@PageTitle("登录 - vaadin-admin")
public class LoginView extends VerticalLayout {

    @Data
    private static class LoginForm {
        private String username;
        private String password;
        private String captcha;
    }

    public LoginView(AuthService authService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%)");

        Icon logo = new Icon(VaadinIcon.SHIELD);
        logo.getStyle()
                .set("width", "42px")
                .set("height", "42px")
                .set("color", "var(--lumo-primary-color)");

        H1 title = new H1("vaadin-admin");
        title.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xxl)");

        Span subtitle = new Span("RBAC 权限管理后台");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        TextField username = new TextField("用户名");
        username.setPrefixComponent(new Icon(VaadinIcon.USER));
        username.setAutofocus(true);
        PasswordField password = new PasswordField("密码");
        password.setPrefixComponent(new Icon(VaadinIcon.LOCK));

        TextField captcha = new TextField("验证码");
        Image captchaImage = new Image("captcha", "验证码，点击刷新");
        captchaImage.setWidth("120px");
        captchaImage.setHeight("40px");
        captchaImage.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("border", "1px solid var(--lumo-contrast-20pct)");
        captchaImage.addClickListener(e -> refreshCaptcha(captchaImage));
        HorizontalLayout captchaRow = new HorizontalLayout(captcha, captchaImage);
        captchaRow.setWidthFull();
        captchaRow.expand(captcha);
        captchaRow.setDefaultVerticalComponentAlignment(Alignment.END);

        Button login = new Button("登 录");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        username.setWidthFull();
        password.setWidthFull();
        login.setWidthFull();

        // Binder 校验：必填项错误红字显示在字段下方
        Binder<LoginForm> binder = new Binder<>(LoginForm.class);
        binder.forField(username)
                .asRequired("请输入用户名")
                .bind(LoginForm::getUsername, LoginForm::setUsername);
        binder.forField(password)
                .asRequired("请输入密码")
                .bind(LoginForm::getPassword, LoginForm::setPassword);
        binder.forField(captcha)
                .asRequired("请输入验证码")
                .bind(LoginForm::getCaptcha, LoginForm::setCaptcha);
        username.setRequiredIndicatorVisible(true);
        password.setRequiredIndicatorVisible(true);
        captcha.setRequiredIndicatorVisible(true);
        LoginForm form = new LoginForm();
        binder.readBean(form);

        VerticalLayout card = new VerticalLayout(logo, title, subtitle, username, password, captchaRow, login);
        card.setWidth("380px");
        card.setAlignItems(Alignment.CENTER);
        card.setPadding(true);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-l)")
                .set("padding", "var(--lumo-space-l)");
        add(card);

        login.addClickShortcut(Key.ENTER);
        login.addClickListener(e -> {
            if (!binder.writeBeanIfValid(form)) {
                return;
            }
            try {
                authService.login(form.getUsername(), form.getPassword(), form.getCaptcha());
                login.getUI().ifPresent(ui -> ui.navigate(HomeView.class));
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                // 验证码一次性有效，登录失败后刷新；同时清空密码和验证码输入
                refreshCaptcha(captchaImage);
                password.clear();
                captcha.clear();
            }
        });
    }

    /** 刷新验证码图片（追加时间戳防止浏览器缓存） */
    private void refreshCaptcha(Image captchaImage) {
        captchaImage.getElement().setProperty("src", "captcha?t=" + System.currentTimeMillis());
    }
}
