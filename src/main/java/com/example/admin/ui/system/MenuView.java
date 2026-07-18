package com.example.admin.ui.system;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.admin.security.RequiresPerm;
import com.example.admin.system.entity.SysMenu;
import com.example.admin.system.service.SysMenuService;
import com.example.admin.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Objects;

@Route(value = "system/menu", layout = MainLayout.class)
@PageTitle("菜单管理 - vaadin-admin")
@RequiresPerm("sys:menu")
public class MenuView extends VerticalLayout {

    private final SysMenuService menuService;
    private final TreeGrid<SysMenu> tree = new TreeGrid<>();

    public MenuView(SysMenuService menuService) {
        this.menuService = menuService;
        setSizeFull();

        Button add = new Button("新增菜单", e -> openDialog(new SysMenu()));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        tree.addHierarchyColumn(SysMenu::getName).setHeader("菜单名称");
        tree.addColumn(m -> typeName(m.getType())).setHeader("类型").setWidth("80px").setFlexGrow(0);
        tree.addColumn(SysMenu::getPath).setHeader("路由地址");
        tree.addColumn(SysMenu::getPerms).setHeader("权限标识");
        tree.addColumn(SysMenu::getSort).setHeader("排序").setWidth("80px").setFlexGrow(0);
        tree.addColumn(m -> Integer.valueOf(0).equals(m.getStatus()) ? "正常" : "停用").setHeader("状态");
        tree.addComponentColumn(this::actionButtons).setHeader("操作").setWidth("180px").setFlexGrow(0);
        tree.setSizeFull();

        add(add, tree);
        refresh();
    }

    private String typeName(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case 0 -> "目录";
            case 1 -> "菜单";
            default -> "按钮";
        };
    }

    private Component actionButtons(SysMenu menu) {
        Button edit = new Button("编辑", e -> openDialog(menu));
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        Button delete = new Button("删除", e -> confirmDelete(menu));
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(edit, delete);
    }

    private void refresh() {
        List<SysMenu> roots = menuService.tree();
        tree.setItems(roots, SysMenu::getChildren);
        tree.expandRecursively(roots, 10);
    }

    private void openDialog(SysMenu menu) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(menu.getId() == null ? "新增菜单" : "编辑菜单");

        // 上级菜单：仅目录和菜单可选，留空表示根节点
        List<SysMenu> candidates = menuService.list(
                Wrappers.<SysMenu>lambdaQuery().le(SysMenu::getType, 1).orderByAsc(SysMenu::getSort));
        candidates.removeIf(m -> Objects.equals(m.getId(), menu.getId()));
        ComboBox<SysMenu> parent = new ComboBox<>("上级菜单");
        parent.setItems(candidates);
        parent.setItemLabelGenerator(SysMenu::getName);
        parent.setClearButtonVisible(true);
        parent.setHelperText("留空为根节点");

        TextField name = new TextField("菜单名称");
        Select<Integer> type = new Select<>();
        type.setLabel("类型");
        type.setItems(0, 1, 2);
        type.setTextRenderer(t -> switch (t) {
            case 0 -> "目录";
            case 1 -> "菜单";
            default -> "按钮";
        });
        TextField path = new TextField("路由地址");
        TextField icon = new TextField("图标");
        icon.setHelperText("VaadinIcon 名称，如 cogs、user、key、list");
        TextField perms = new TextField("权限标识");
        perms.setHelperText("如 sys:user，与 @RequiresPerm 对应");
        IntegerField sort = new IntegerField("排序");
        Checkbox enabled = new Checkbox("启用");

        // Binder 绑定与校验：校验失败时错误信息红色显示在字段下方
        Binder<SysMenu> binder = new Binder<>(SysMenu.class);
        binder.forField(parent)
                .withConverter(m -> m == null ? 0L : m.getId(),
                        id -> candidates.stream()
                                .filter(c -> Objects.equals(c.getId(), id))
                                .findFirst().orElse(null))
                .bind(SysMenu::getParentId, SysMenu::setParentId);
        binder.forField(name).asRequired("菜单名称不能为空").bind(SysMenu::getName, SysMenu::setName);
        binder.forField(type).asRequired("请选择类型").bind(SysMenu::getType, SysMenu::setType);
        binder.bind(path, SysMenu::getPath, SysMenu::setPath);
        binder.bind(icon, SysMenu::getIcon, SysMenu::setIcon);
        binder.bind(perms, SysMenu::getPerms, SysMenu::setPerms);
        binder.forField(sort)
                .withConverter(v -> v == null ? 0 : v, v -> v)
                .bind(SysMenu::getSort, SysMenu::setSort);
        binder.forField(enabled)
                .withConverter(checked -> checked ? 0 : 1, status -> !Integer.valueOf(1).equals(status))
                .bind(SysMenu::getStatus, SysMenu::setStatus);
        name.setRequiredIndicatorVisible(true);

        // 新增时的默认值
        if (menu.getType() == null) {
            menu.setType(1);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        binder.readBean(menu);

        FormLayout form = new FormLayout(parent, name, type, path, icon, perms, sort, enabled);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialog.add(form);

        Button cancel = new Button("取消", e -> dialog.close());
        Button save = new Button("保存", e -> {
            if (!binder.writeBeanIfValid(menu)) {
                return;
            }
            menuService.saveOrUpdate(menu);
            dialog.close();
            refresh();
            Notification.show("保存成功");
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void confirmDelete(SysMenu menu) {
        ConfirmDialog dialog = new ConfirmDialog("删除菜单",
                "确定删除菜单「" + menu.getName() + "」吗？", "删除", e -> {
            try {
                menuService.deleteMenu(menu.getId());
                refresh();
                Notification.show("删除成功");
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("取消");
        dialog.open();
    }
}
