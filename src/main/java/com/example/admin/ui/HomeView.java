package com.example.admin.ui;

import cn.hutool.core.date.DateUtil;
import com.example.admin.security.AuthService;
import com.example.admin.system.entity.SysOperLog;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysMenuService;
import com.example.admin.system.service.SysOperLogService;
import com.example.admin.system.service.SysRoleService;
import com.example.admin.system.service.SysUserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Date;

/** 首页 Dashboard：欢迎横幅 + 统计卡片 + 最近操作 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("首页 - vaadin-admin")
public class HomeView extends VerticalLayout {

    public HomeView(AuthService authService, SysUserService userService, SysRoleService roleService,
                    SysMenuService menuService, SysOperLogService operLogService) {
        SysUser user = authService.getCurrentUser();
        String name = user.getNickname() == null ? user.getUsername() : user.getNickname();
        setSizeFull();

        // 欢迎横幅
        H2 greeting = new H2("你好，" + name);
        Paragraph dateInfo = new Paragraph("今天是 " + DateUtil.format(new Date(), "yyyy年MM月dd日 EEEE")
                + "，欢迎使用 vaadin-admin 管理后台。");
        VerticalLayout banner = new VerticalLayout(greeting, dateInfo);
        banner.addClassName("hero-banner");
        banner.setWidthFull();

        // 统计卡片
        HorizontalLayout stats = new HorizontalLayout(
                statCard(VaadinIcon.USERS, "var(--lumo-primary-color)", "用户总数", userService.count()),
                statCard(VaadinIcon.KEY, "var(--lumo-success-color)", "角色总数", roleService.count()),
                statCard(VaadinIcon.LIST, "#f59e0b", "菜单总数", menuService.count()),
                statCard(VaadinIcon.FILE_TEXT, "#8b5cf6", "操作日志", operLogService.count()));
        stats.setWidthFull();
        stats.getChildren().forEach(stats::expand);

        // 最近操作
        H3 recentTitle = new H3("最近操作");
        Grid<SysOperLog> recentGrid = new Grid<>(SysOperLog.class, false);
        recentGrid.addColumn(SysOperLog::getUsername).setHeader("操作人");
        recentGrid.addColumn(SysOperLog::getOperation).setHeader("操作");
        recentGrid.addComponentColumn(l -> statusBadge(l.getStatus())).setHeader("状态");
        recentGrid.addColumn(l -> DateUtil.format(l.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("操作时间");
        recentGrid.setItems(operLogService.listLatest(10));
        recentGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        recentGrid.setAllRowsVisible(true);
        VerticalLayout recentCard = new VerticalLayout(recentTitle, recentGrid);
        recentCard.setWidthFull();
        recentCard.addClassName("info-card");

        add(banner, stats, recentCard);
    }

    /** 统计卡片：彩色图标 + 数值 + 标签 */
    private Component statCard(VaadinIcon vaadinIcon, String color, String label, long value) {
        Icon icon = new Icon(vaadinIcon);
        icon.getStyle().set("color", "#fff").set("width", "22px").set("height", "22px");
        Span iconBox = new Span(icon);
        iconBox.getStyle().set("background", color);
        iconBox.addClassName("stat-icon-box");

        Span number = new Span(String.valueOf(value));
        number.addClassName("stat-number");
        Span labelEl = new Span(label);
        labelEl.addClassName("stat-label");
        VerticalLayout text = new VerticalLayout(number, labelEl);
        text.setPadding(false);
        text.setSpacing(false);

        HorizontalLayout card = new HorizontalLayout(iconBox, text);
        card.setAlignItems(Alignment.CENTER);
        card.addClassName("stat-card");
        return card;
    }

    /** 状态徽标：绿色成功 / 红色失败 */
    private Component statusBadge(Integer status) {
        boolean success = Integer.valueOf(0).equals(status);
        Span badge = new Span(success ? "成功" : "失败");
        badge.getElement().getThemeList().add(success ? "badge success" : "badge error");
        return badge;
    }
}
