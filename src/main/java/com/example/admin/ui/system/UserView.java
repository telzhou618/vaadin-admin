package com.example.admin.ui.system;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.security.RequiresPerm;
import com.example.admin.system.entity.SysRole;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysRoleService;
import com.example.admin.system.service.SysUserService;
import com.example.admin.ui.MainLayout;
import com.example.admin.ui.PaginationBar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "system/user", layout = MainLayout.class)
@PageTitle("用户管理 - vaadin-admin")
@RequiresPerm("sys:user")
public class UserView extends VerticalLayout {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final Grid<SysUser> grid = new Grid<>(SysUser.class, false);
    private final TextField keyword = new TextField();
    private final PaginationBar paginationBar = new PaginationBar(this::loadPage);

    public UserView(SysUserService userService, SysRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
        setSizeFull();

        H2 title = new H2("用户管理");
        title.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");

        keyword.setPlaceholder("用户名 / 昵称");
        keyword.setClearButtonVisible(true);
        keyword.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        keyword.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        keyword.addKeyPressListener(Key.ENTER, e -> paginationBar.reset());
        Button search = new Button("搜索", e -> paginationBar.reset());
        search.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button add = new Button("新增用户", new Icon(VaadinIcon.PLUS), e -> openDialog(new SysUser(), List.of()));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(title, keyword, search, add);
        toolbar.setWidthFull();
        toolbar.expand(title);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        grid.addColumn(SysUser::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addComponentColumn(u -> avatar(u.getAvatar())).setHeader("头像").setWidth("70px").setFlexGrow(0);
        grid.addColumn(SysUser::getUsername).setHeader("用户名");
        grid.addColumn(SysUser::getNickname).setHeader("昵称");
        grid.addColumn(SysUser::getPhone).setHeader("手机号");
        grid.addColumn(u -> genderText(u.getGender())).setHeader("性别").setWidth("70px").setFlexGrow(0);
        grid.addColumn(u -> u.getBirthday() == null ? "" : u.getBirthday().toString())
                .setHeader("生日");
        grid.addColumn(SysUser::getEmail).setHeader("邮箱");
        grid.addComponentColumn(u -> statusBadge(u.getStatus())).setHeader("状态").setWidth("90px").setFlexGrow(0);
        grid.addColumn(u -> DateUtil.format(u.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("创建时间");
        grid.addComponentColumn(this::actionButtons).setHeader("操作").setWidth("230px").setFlexGrow(0);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        add(toolbar, grid, paginationBar);
        refresh();
    }

    private Component actionButtons(SysUser user) {
        Button edit = new Button("编辑", e -> openDialog(user, userService.getRoleIds(user.getId())));
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        boolean enabled = Integer.valueOf(0).equals(user.getStatus());
        Button toggle = new Button(enabled ? "停用" : "启用", e -> {
            user.setStatus(enabled ? 1 : 0);
            userService.updateById(user);
            refresh();
        });
        toggle.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Button delete = new Button("删除", e -> confirmDelete(user));
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(edit, toggle, delete);
    }

    /** 状态徽标：绿色正常 / 红色停用 */
    private Component statusBadge(Integer status) {
        boolean enabled = Integer.valueOf(0).equals(status);
        Span badge = new Span(enabled ? "正常" : "停用");
        badge.getElement().getThemeList().add(enabled ? "badge success" : "badge error");
        return badge;
    }

    /** 头像：有地址显示圆形图片，无地址显示默认图标 */
    private Component avatar(String url) {
        if (StrUtil.isBlank(url)) {
            Icon icon = new Icon(VaadinIcon.USER);
            icon.getStyle().set("width", "28px").set("height", "28px")
                    .set("color", "var(--lumo-contrast-50pct)");
            return icon;
        }
        Image image = new Image(url, "头像");
        image.setWidth("28px");
        image.setHeight("28px");
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

    private void refresh() {
        paginationBar.refresh();
    }

    private void loadPage(int page, int pageSize) {
        Page<SysUser> result = userService.pageUsers(keyword.getValue(), page, pageSize);
        grid.setItems(result.getRecords());
        paginationBar.setTotal(result.getTotal());
    }

    private void openDialog(SysUser user, List<Long> roleIds) {
        boolean isNew = user.getId() == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "新增用户" : "编辑用户");

        TextField username = new TextField("用户名");
        TextField nickname = new TextField("昵称");
        TextField email = new TextField("邮箱");
        TextField phone = new TextField("手机号");
        TextField avatar = new TextField("头像地址");
        RadioButtonGroup<Integer> gender = new RadioButtonGroup<>("性别");
        gender.setItems(0, 1, 2);
        gender.setItemLabelGenerator(this::genderText);
        DatePicker birthday = new DatePicker("生日");
        PasswordField password = new PasswordField(isNew ? "初始密码" : "重置密码（留空则不修改）");
        Checkbox enabled = new Checkbox("启用");

        List<SysRole> allRoles = roleService.list();
        MultiSelectComboBox<SysRole> roles = new MultiSelectComboBox<>("角色");
        roles.setItems(allRoles);
        roles.setItemLabelGenerator(SysRole::getName);
        roles.setValue(allRoles.stream()
                .filter(r -> roleIds.contains(r.getId()))
                .collect(Collectors.toSet()));

        // Binder 绑定与校验：校验失败时错误信息红色显示在字段下方
        Binder<SysUser> binder = new Binder<>(SysUser.class);
        binder.forField(username)
                .asRequired("用户名不能为空")
                .bind(SysUser::getUsername, SysUser::setUsername);
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
        var passwordBinding = binder.forField(password);
        if (isNew) {
            passwordBinding.asRequired("请设置初始密码");
        }
        passwordBinding.bind(SysUser::getPassword, SysUser::setPassword);
        binder.forField(enabled)
                .withConverter(checked -> checked ? 0 : 1, status -> !Integer.valueOf(1).equals(status))
                .bind(SysUser::getStatus, SysUser::setStatus);

        username.setRequiredIndicatorVisible(true);
        if (isNew) {
            password.setRequiredIndicatorVisible(true);
        }

        binder.readBean(user);
        // 编辑时实体里是 BCrypt 密文，清空密码框；留空保存即不修改密码
        password.clear();

        FormLayout form = new FormLayout(username, nickname, email, phone, gender, birthday, avatar, password, enabled, roles);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialog.add(form);

        Button cancel = new Button("取消", e -> dialog.close());
        Button save = new Button("保存", e -> {
            if (!binder.writeBeanIfValid(user)) {
                return;
            }
            try {
                userService.saveUser(user, roles.getValue().stream().map(SysRole::getId).toList());
                dialog.close();
                refresh();
                Notification.show("保存成功");
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void confirmDelete(SysUser user) {
        ConfirmDialog dialog = new ConfirmDialog("删除用户",
                "确定删除用户「" + user.getUsername() + "」吗？", "删除", e -> {
            userService.deleteUser(user.getId());
            refresh();
            Notification.show("删除成功");
        });
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("取消");
        dialog.open();
    }
}
