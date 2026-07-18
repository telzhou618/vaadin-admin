package com.example.admin.ui.system;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.admin.security.RequiresPerm;
import com.example.admin.system.entity.SysRole;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysRoleService;
import com.example.admin.system.service.SysUserService;
import com.example.admin.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
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

    public UserView(SysUserService userService, SysRoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
        setSizeFull();

        keyword.setPlaceholder("用户名 / 昵称");
        keyword.setClearButtonVisible(true);
        keyword.addKeyPressListener(Key.ENTER, e -> refresh());
        Button search = new Button("搜索", e -> refresh());
        Button add = new Button("新增用户", e -> openDialog(new SysUser(), List.of()));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout toolbar = new HorizontalLayout(keyword, search, add);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        grid.addColumn(SysUser::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(SysUser::getUsername).setHeader("用户名");
        grid.addColumn(SysUser::getNickname).setHeader("昵称");
        grid.addColumn(SysUser::getEmail).setHeader("邮箱");
        grid.addColumn(u -> Integer.valueOf(0).equals(u.getStatus()) ? "正常" : "停用").setHeader("状态");
        grid.addColumn(u -> DateUtil.format(u.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("创建时间");
        grid.addComponentColumn(this::actionButtons).setHeader("操作").setWidth("230px").setFlexGrow(0);
        grid.setSizeFull();

        add(toolbar, grid);
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

    private void refresh() {
        grid.setItems(userService.listUsers(keyword.getValue()));
    }

    private void openDialog(SysUser user, List<Long> roleIds) {
        boolean isNew = user.getId() == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "新增用户" : "编辑用户");

        TextField username = new TextField("用户名");
        username.setValue(user.getUsername() == null ? "" : user.getUsername());
        TextField nickname = new TextField("昵称");
        nickname.setValue(user.getNickname() == null ? "" : user.getNickname());
        TextField email = new TextField("邮箱");
        email.setValue(user.getEmail() == null ? "" : user.getEmail());
        PasswordField password = new PasswordField(isNew ? "初始密码" : "重置密码（留空则不修改）");
        Checkbox enabled = new Checkbox("启用");
        enabled.setValue(!Integer.valueOf(1).equals(user.getStatus()));

        List<SysRole> allRoles = roleService.list();
        MultiSelectComboBox<SysRole> roles = new MultiSelectComboBox<>("角色");
        roles.setItems(allRoles);
        roles.setItemLabelGenerator(SysRole::getName);
        roles.setValue(allRoles.stream()
                .filter(r -> roleIds.contains(r.getId()))
                .collect(Collectors.toSet()));

        FormLayout form = new FormLayout(username, nickname, email, password, enabled, roles);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialog.add(form);

        Button cancel = new Button("取消", e -> dialog.close());
        Button save = new Button("保存", e -> {
            if (StrUtil.isBlank(username.getValue())) {
                Notification.show("用户名不能为空");
                return;
            }
            if (isNew && StrUtil.isBlank(password.getValue())) {
                Notification.show("请设置初始密码");
                return;
            }
            user.setUsername(username.getValue().trim());
            user.setNickname(StrUtil.trimToNull(nickname.getValue()));
            user.setEmail(StrUtil.trimToNull(email.getValue()));
            user.setPassword(StrUtil.trimToNull(password.getValue()));
            user.setStatus(enabled.getValue() ? 0 : 1);
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
