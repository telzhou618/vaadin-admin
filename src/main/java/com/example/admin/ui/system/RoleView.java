package com.example.admin.ui.system;

import cn.hutool.core.date.DateUtil;
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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
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

        H2 title = new H2("角色管理");
        title.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");
        Button add = new Button("新增角色", new Icon(VaadinIcon.PLUS), e -> openDialog(new SysRole()));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(title, add);
        toolbar.setWidthFull();
        toolbar.expand(title);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        grid.addColumn(SysRole::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(SysRole::getCode).setHeader("角色编码");
        grid.addColumn(SysRole::getName).setHeader("角色名称");
        grid.addColumn(SysRole::getDescription).setHeader("描述");
        grid.addComponentColumn(r -> statusBadge(r.getStatus())).setHeader("状态").setWidth("90px").setFlexGrow(0);
        grid.addColumn(r -> DateUtil.format(r.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("创建时间");
        grid.addComponentColumn(this::actionButtons).setHeader("操作").setWidth("180px").setFlexGrow(0);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        add(toolbar, grid);
        refresh();
    }

    private Component actionButtons(SysRole role) {
        Button edit = new Button("编辑", e -> openDialog(role));
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        Button delete = new Button("删除", e -> confirmDelete(role));
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(edit, delete);
    }

    /** 状态徽标：绿色正常 / 红色停用 */
    private Component statusBadge(Integer status) {
        boolean enabled = Integer.valueOf(0).equals(status);
        Span badge = new Span(enabled ? "正常" : "停用");
        badge.getElement().getThemeList().add(enabled ? "badge success" : "badge error");
        return badge;
    }

    private void refresh() {
        grid.setItems(roleService.list());
    }

    private void openDialog(SysRole role) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(role.getId() == null ? "新增角色" : "编辑角色");

        TextField code = new TextField("角色编码");
        code.setHelperText("如 admin，编码为 admin 的角色拥有全部权限");
        TextField name = new TextField("角色名称");
        TextField description = new TextField("描述");
        Checkbox enabled = new Checkbox("启用");

        // Binder 绑定与校验：校验失败时错误信息红色显示在字段下方
        Binder<SysRole> binder = new Binder<>(SysRole.class);
        binder.forField(code).asRequired("角色编码不能为空").bind(SysRole::getCode, SysRole::setCode);
        binder.forField(name).asRequired("角色名称不能为空").bind(SysRole::getName, SysRole::setName);
        binder.bind(description, SysRole::getDescription, SysRole::setDescription);
        binder.forField(enabled)
                .withConverter(checked -> checked ? 0 : 1, status -> !Integer.valueOf(1).equals(status))
                .bind(SysRole::getStatus, SysRole::setStatus);
        code.setRequiredIndicatorVisible(true);
        name.setRequiredIndicatorVisible(true);
        binder.readBean(role);

        // 菜单树勾选分配权限
        TreeGrid<SysMenu> menuTree = new TreeGrid<>();
        menuTree.addHierarchyColumn(SysMenu::getName).setHeader("菜单权限");
        menuTree.setSelectionMode(Grid.SelectionMode.MULTI);
        menuTree.setHeight("320px");
        menuTree.addThemeVariants(GridVariant.LUMO_COMPACT);
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
            if (!binder.writeBeanIfValid(role)) {
                return;
            }
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
