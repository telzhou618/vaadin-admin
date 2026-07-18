package com.example.admin.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.admin.security.AuthService;
import com.example.admin.system.entity.SysMenu;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysUserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

/** 主布局：顶部栏 + 按当前用户菜单动态生成的侧边导航 */
public class MainLayout extends AppLayout {

    public MainLayout(AuthService authService, SysUserService userService) {
        Icon logoIcon = new Icon(VaadinIcon.SHIELD);
        logoIcon.getStyle().set("color", "var(--lumo-primary-color)");
        H1 title = new H1("vaadin-admin");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
        HorizontalLayout logo = new HorizontalLayout(logoIcon, title);
        logo.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        SysUser currentUser = authService.getCurrentUser();
        String name = currentUser.getNickname() == null ? currentUser.getUsername() : currentUser.getNickname();
        Span nickname = new Span(new Icon(VaadinIcon.USER), new Span(name));
        nickname.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        Button changePwd = new Button("修改密码", new Icon(VaadinIcon.KEY),
                e -> new ChangePasswordDialog(userService).open());
        changePwd.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button logout = new Button("退出", new Icon(VaadinIcon.SIGN_OUT), e -> {
            authService.logout();
            e.getSource().getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // 侧边菜单收起 / 展开切换
        Button menuToggle = new Button(new Icon(VaadinIcon.MENU), e -> setDrawerOpened(!isDrawerOpened()));
        menuToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(menuToggle, logo, nickname, changePwd, logout);
        header.setWidthFull();
        header.setPadding(true);
        header.expand(logo);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        addToNavbar(header);

        SideNav nav = new SideNav();
        SideNavItem home = new SideNavItem("首页", HomeView.class);
        home.setPrefixComponent(new Icon(VaadinIcon.HOME));
        nav.addItem(home);
        authService.getCurrentUserMenus().forEach(menu -> nav.addItem(toNavItem(menu)));
        addToDrawer(nav);
    }

    /** 菜单实体转导航项（目录渲染为可展开分组） */
    private SideNavItem toNavItem(SysMenu menu) {
        SideNavItem item = menu.getType() != null && menu.getType() == 1
                ? new SideNavItem(menu.getName(), menu.getPath())
                : new SideNavItem(menu.getName());
        Component icon = iconOf(menu.getIcon());
        if (icon != null) {
            item.setPrefixComponent(icon);
        }
        if (CollUtil.isNotEmpty(menu.getChildren())) {
            menu.getChildren().forEach(child -> item.addItem(toNavItem(child)));
        }
        return item;
    }

    private Component iconOf(String icon) {
        if (StrUtil.isBlank(icon)) {
            return null;
        }
        try {
            return new Icon(VaadinIcon.valueOf(icon.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
