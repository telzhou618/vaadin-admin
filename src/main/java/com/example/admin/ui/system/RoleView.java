package com.example.admin.ui.system;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.admin.security.RequiresPerm;
import com.example.admin.system.entity.SysMenu;
import com.example.admin.system.entity.SysRole;
import com.example.admin.system.service.SysMenuService;
import com.example.admin.system.service.SysRoleService;
import com.example.admin.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Route(value = "system/role", layout = MainLayout.class)
@PageTitle("角色管理 - vaadin-admin")
@RequiresPerm("sys:role")
public class RoleView extends VerticalLayout {

    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final Grid<SysRole> grid = new Grid<>(SysRole.class, false);

    public RoleView(SysRoleService roleService, SysMenuService menuService) {
        this.roleService = roleService;
        this.menuService = menuService;
        setSizeFull();

        Button add = new Button("新增角色", e -> openDialog(new SysRole()));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid.addColumn(SysRole::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(SysRole::getCode).setHeader("角色编码");
        grid.addColumn(SysRole::getName).setHeader("角色名称");
        grid.addColumn(SysRole::getDescription).setHeader("描述");
        grid.addColumn(r -> Integer.valueOf(0).equals(r.getStatus()) ? "正常" : "停用").setHeader("状态");
        grid.addColumn(r -> DateUtil.format(r.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("创建时间");
        grid.addComponentColumn(this::actionButtons).setHeader("操作").setWidth("180px").setFlexGrow(0);
        grid.setSizeFull();

        add(add, grid);
        refresh();
    }

    private Component actionButtons(SysRole role) {
        Button edit = new Button("编辑", e -> openDialog(role));
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        Button delete = new Button("删除", e -> confirmDelete(role));
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(edit, delete);
    }

    private void refresh() {
        grid.setItems(roleService.list());
    }

    private void openDialog(SysRole role) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(role.getId() == null ? "新增角色" : "编辑角色");

        TextField code = new TextField("角色编码");
        code.setValue(role.getCode() == null ? "" : role.getCode());
        code.setHelperText("如 admin，编码为 admin 的角色拥有全部权限");
        TextField name = new TextField("角色名称");
        name.setValue(role.getName() == null ? "" : role.getName());
        TextField description = new TextField("描述");
        description.setValue(role.getDescription() == null ? "" : role.getDescription());
        Checkbox enabled = new Checkbox("启用");
        enabled.setValue(!Integer.valueOf(1).equals(role.getStatus()));

        // 菜单树勾选分配权限
        TreeGrid<SysMenu> menuTree = new TreeGrid<>();
        menuTree.addHierarchyColumn(SysMenu::getName).setHeader("菜单权限");
        menuTree.setSelectionMode(Grid.SelectionMode.MULTI);
        menuTree.setHeight("320px");
        List<SysMenu> roots = menuService.tree();
        menuTree.setItems(roots, SysMenu::getChildren);
        menuTree.expandRecursively(roots, 10);
        if (role.getId() != null) {
            selectMenus(menuTree, roots, new HashSet<>(roleService.getMenuIds(role.getId())));
        }

        FormLayout form = new FormLayout(code, name, description, enabled);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialog.add(form, menuTree);

        Button cancel = new Button("取消", e -> dialog.close());
        Button save = new Button("保存", e -> {
            if (StrUtil.isBlank(code.getValue()) || StrUtil.isBlank(name.getValue())) {
                Notification.show("角色编码和名称不能为空");
                return;
            }
            role.setCode(code.getValue().trim());
            role.setName(name.getValue().trim());
            role.setDescription(StrUtil.trimToNull(description.getValue()));
            role.setStatus(enabled.getValue() ? 0 : 1);
            try {
                roleService.saveRole(role,
                        menuTree.getSelectedItems().stream().map(SysMenu::getId).toList());
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

    /** 勾选角色已分配的菜单（须使用树中的同一批实例） */
    private void selectMenus(TreeGrid<SysMenu> tree, List<SysMenu> nodes, Set<Long> checked) {
        if (nodes == null) {
            return;
        }
        for (SysMenu node : nodes) {
            if (checked.contains(node.getId())) {
                tree.select(node);
            }
            selectMenus(tree, node.getChildren(), checked);
        }
    }

    private void confirmDelete(SysRole role) {
        ConfirmDialog dialog = new ConfirmDialog("删除角色",
                "确定删除角色「" + role.getName() + "」吗？", "删除", e -> {
            roleService.deleteRole(role.getId());
            refresh();
            Notification.show("删除成功");
        });
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("取消");
        dialog.open();
    }
}
