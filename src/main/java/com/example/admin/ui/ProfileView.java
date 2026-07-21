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

/** 个人中心：展示当前登录用户的详细信息，可修改个人资料（用户名和 ID 除外）和密码 */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("个人中心 - vaadin-admin")
public class ProfileView extends VerticalLayout {

    private final AuthService authService;
    private final SysUserService userService;
    private final VerticalLayout avatarBox = new VerticalLayout();
    private final VerticalLayout details = new VerticalLayout();

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

        avatarBox.setPadding(false);
        avatarBox.setSpacing(false);
        details.setPadding(false);
        details.getStyle().set("row-gap", "var(--lumo-space-s)");
        HorizontalLayout card = new HorizontalLayout(avatarBox, details);
        card.setWidthFull();
        card.setPadding(true);
        card.setDefaultVerticalComponentAlignment(Alignment.START);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("gap", "var(--lumo-space-xl)");

        add(toolbar, card);
        showDetails();
    }

    /** 重新加载并展示当前用户信息 */
    private void showDetails() {
        SysUser user = authService.getCurrentUser();
        avatarBox.removeAll();
        avatarBox.add(avatar(user.getAvatar(), "96px"));

        details.removeAll();
        details.add(
                row("ID", String.valueOf(user.getId())),
                row("用户名", user.getUsername()),
                row("昵称", user.getNickname()),
                row("性别", genderText(user.getGender())),
                row("手机号", user.getPhone()),
                row("邮箱", user.getEmail()),
                row("生日", user.getBirthday() == null ? "" : user.getBirthday().toString()),
                row("注册时间", DateUtil.format(user.getCreateTime(), "yyyy-MM-dd HH:mm:ss")));
    }

    /** 头像：有地址显示圆形图片，无地址显示默认图标 */
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

    /** 详情行：灰色标签 + 值（空值显示 —） */
    private Component row(String label, String value) {
        Span labelEl = new Span(label);
        labelEl.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("width", "5em")
                .set("flex-shrink", "0");
        Span valueEl = new Span(StrUtil.isBlank(value) ? "—" : value);
        HorizontalLayout row = new HorizontalLayout(labelEl, valueEl);
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        return row;
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

    /** 编辑个人资料：用户名和 ID 不在表单中，服务端也按字段白名单更新 */
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
