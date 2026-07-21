package com.example.admin.ui;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.lang.Validator;
import com.example.admin.security.AuthService;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysUserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * 个人中心：展示当前登录用户的详细信息，可修改个人资料（用户名和 ID 除外）和密码
 */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("个人中心 - vaadin-admin")
public class ProfileView extends VerticalLayout {

    private final AuthService authService;
    private final SysUserService userService;
    private final VerticalLayout content = new VerticalLayout();

    public ProfileView(AuthService authService, SysUserService userService) {
        this.authService = authService;
        this.userService = userService;
        setSizeFull();

        H2 title = new H2("个人中心");
        title.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");

        Button edit = new Button("编辑资料", new Icon(VaadinIcon.EDIT), e -> openEditDialog());
        edit.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button changePwd = new Button("修改密码", new Icon(VaadinIcon.KEY),
                e -> new ChangePasswordDialog(userService).open());
        changePwd.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(title, edit, changePwd);
        toolbar.setWidthFull();
        toolbar.expand(title);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // 内容占满页面并垂直居中
        content.setWidthFull();
        content.setPadding(false);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("gap", "var(--lumo-space-l)");
        VerticalLayout center = new VerticalLayout(content);
        center.setSizeFull();
        center.setPadding(false);
        center.setAlignItems(Alignment.CENTER);
        center.setJustifyContentMode(JustifyContentMode.CENTER);

        add(toolbar, center);
        expand(center);
        showDetails();
    }

    /**
     * 重新加载并展示当前用户信息
     */
    private void showDetails() {
        SysUser user = authService.getCurrentUser();
        content.removeAll();
        content.add(hero(user), infoCard(user));
    }

    /**
     * 顶部区域：渐变横幅 + 悬空圆形头像 + 昵称与用户名
     */
    private Component hero(SysUser user) {
        Div banner = new Div();
        banner.setWidthFull();
        banner.setHeight("120px");
        banner.getStyle()
                .set("background", "linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%)")
                .set("border-radius", "var(--lumo-border-radius-l)");

        Component avatar = avatar(user.getAvatar(), "120px");
        avatar.getStyle()
                .set("margin-top", "-64px")
                .set("border", "4px solid var(--lumo-base-color)")
                .set("border-radius", "50%")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("box-sizing", "border-box")
                .set("background", "var(--lumo-contrast-5pct)");

        String displayName = user.getNickname() == null ? user.getUsername() : user.getNickname();
        H2 nickname = new H2(displayName);
        nickname.getStyle().set("margin", "0");
        Span username = new Span("@" + user.getUsername());
        username.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        VerticalLayout nameBox = new VerticalLayout(nickname, username);
        nameBox.setPadding(false);
        nameBox.setSpacing(false);
        nameBox.setAlignItems(Alignment.CENTER);
        nameBox.getStyle().set("margin-top", "var(--lumo-space-s)").set("row-gap", "var(--lumo-space-xs)");

        VerticalLayout hero = new VerticalLayout(banner, avatar, nameBox);
        hero.setWidthFull();
        hero.setPadding(false);
        hero.setSpacing(false);
        hero.setAlignItems(Alignment.CENTER);
        return hero;
    }

    /**
     * 资料卡片：每个字段一行（图标 + 标签 + 值），行间细分隔线
     */
    private Component infoCard(SysUser user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("720px");
        card.setMaxWidth("100%");
        card.setPadding(true);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("row-gap", "var(--lumo-space-s)");
        card.add(
                fieldRow(VaadinIcon.HASH, "ID", String.valueOf(user.getId()), false),
                fieldRow(VaadinIcon.USER, "用户名", user.getUsername(), false),
                fieldRow(VaadinIcon.TAG, "昵称", user.getNickname(), false),
                fieldRow(genderIcon(user.getGender()), "性别", genderText(user.getGender()), false),
                fieldRow(VaadinIcon.MOBILE, "手机号", user.getPhone(), false),
                fieldRow(VaadinIcon.ENVELOPE, "邮箱", user.getEmail(), false),
                fieldRow(VaadinIcon.CALENDAR, "生日",
                        user.getBirthday() == null ? "" : user.getBirthday().toString(), false),
                fieldRow(VaadinIcon.CLOCK, "注册时间",
                        DateUtil.format(user.getCreateTime(), "yyyy-MM-dd HH:mm:ss"), true));
        return card;
    }

    /**
     * 字段行：主色图标 + 灰色标签 + 值（空值显示 —），行间分隔线
     */
    private Component fieldRow(VaadinIcon icon, String label, String value, boolean last) {
        Icon leading = new Icon(icon);
        leading.getStyle()
                .set("width", "18px")
                .set("height", "18px")
                .set("flex-shrink", "0")
                .set("color", "var(--lumo-primary-color)");
        Span labelEl = new Span(label);
        labelEl.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("width", "5.5em")
                .set("flex-shrink", "0");
        Span valueEl = new Span(StrUtil.isBlank(value) ? "—" : value);
        valueEl.getStyle().set("font-weight", "500");
        HorizontalLayout row = new HorizontalLayout(leading, labelEl, valueEl);
        row.setWidthFull();
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        if (!last) {
            row.getStyle()
                    .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                    .set("padding-bottom", "var(--lumo-space-s)");
        }
        return row;
    }

    /**
     * 性别图标：男 / 女 / 保密
     */
    private VaadinIcon genderIcon(Integer gender) {
        if (Integer.valueOf(0).equals(gender)) {
            return VaadinIcon.MALE;
        }
        if (Integer.valueOf(1).equals(gender)) {
            return VaadinIcon.FEMALE;
        }
        return VaadinIcon.INFO;
    }

    /**
     * 头像：有地址显示圆形图片，无地址显示默认图标
     */
    private Component avatar(String url, String size) {
        if (StrUtil.isBlank(url)) {
            Icon icon = new Icon(VaadinIcon.USER);
            icon.getStyle().set("width", size).set("height", size)
                    .set("color", "var(--lumo-contrast-50pct)");
            return icon;
        }
        Image image = new Image(url, "头像");
        image.setWidth(size);
        image.setHeight(size);
        image.getStyle().set("border-radius", "50%").set("object-fit", "cover");
        return image;
    }

    private String genderText(Integer gender) {
        if (gender == null) {
            return "";
        }
        return switch (gender) {
            case 0 -> "男";
            case 1 -> "女";
            default -> "保密";
        };
    }

    /**
     * 编辑个人资料：用户名和 ID 不在表单中，服务端也按字段白名单更新
     */
    private void openEditDialog() {
        SysUser user = authService.getCurrentUser();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("编辑资料");

        TextField nickname = new TextField("昵称");
        TextField email = new TextField("邮箱");
        TextField phone = new TextField("手机号");
        TextField avatar = new TextField("头像地址");
        RadioButtonGroup<Integer> gender = new RadioButtonGroup<>("性别");
        gender.setItems(0, 1, 2);
        gender.setItemLabelGenerator(this::genderText);
        DatePicker birthday = new DatePicker("生日");

        // Binder 绑定与校验：校验失败时错误信息红色显示在字段下方
        Binder<SysUser> binder = new Binder<>(SysUser.class);
        binder.bind(nickname, SysUser::getNickname, SysUser::setNickname);
        binder.forField(email)
                .asRequired("邮箱不能为空")
                .withValidator(e -> StrUtil.isBlank(e) || Validator.isEmail(e), "邮箱格式不正确")
                .bind(SysUser::getEmail, SysUser::setEmail);
        binder.forField(phone)
                .withValidator(p -> StrUtil.isBlank(p) || p.matches("\\d{11}"), "请输入 11 位手机号")
                .bind(SysUser::getPhone, SysUser::setPhone);
        binder.bind(avatar, SysUser::getAvatar, SysUser::setAvatar);
        binder.forField(gender).bind(SysUser::getGender, SysUser::setGender);
        binder.bind(birthday, SysUser::getBirthday, SysUser::setBirthday);
        email.setRequiredIndicatorVisible(true);

        binder.readBean(user);

        FormLayout form = new FormLayout(nickname, email, phone, gender, birthday, avatar);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialog.add(form);

        Button cancel = new Button("取消", e -> dialog.close());
        Button save = new Button("保存", e -> {
            if (!binder.writeBeanIfValid(user)) {
                return;
            }
            try {
                userService.updateProfile(user);
                dialog.close();
                showDetails();
                Notification.show("保存成功");
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }
}
