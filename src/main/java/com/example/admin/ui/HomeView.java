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
        greeting.getStyle().set("margin", "0");
        Paragraph dateInfo = new Paragraph("今天是 " + DateUtil.format(new Date(), "yyyy年MM月dd日 EEEE")
                + "，欢迎使用 vaadin-admin 管理后台。");
        dateInfo.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0");
        VerticalLayout banner = new VerticalLayout(greeting, dateInfo);
        banner.getStyle()
                .set("background", "linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)");
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
        recentTitle.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-l)");
        Grid<SysOperLog> recentGrid = new Grid<>(SysOperLog.class, false);
        recentGrid.addColumn(SysOperLog::getUsername).setHeader("操作人");
        recentGrid.addColumn(SysOperLog::getOperation).setHeader("操作");
        recentGrid.addComponentColumn(l -> statusBadge(l.getStatus())).setHeader("状态")
                .setWidth("90px").setFlexGrow(0);
        recentGrid.addColumn(l -> DateUtil.format(l.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).setHeader("操作时间");
        recentGrid.setItems(operLogService.listLatest(10));
        recentGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        recentGrid.setAllRowsVisible(true);
        VerticalLayout recentCard = new VerticalLayout(recentTitle, recentGrid);
        recentCard.setWidthFull();
        recentCard.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-m)");

        add(banner, stats, recentCard);
    }

    /** 统计卡片：彩色图标 + 数值 + 标签 */
    private Component statCard(VaadinIcon vaadinIcon, String color, String label, long value) {
        Icon icon = new Icon(vaadinIcon);
        icon.getStyle().set("color", "#fff").set("width", "22px").set("height", "22px");
        Span iconBox = new Span(icon);
        iconBox.getStyle()
                .set("background", color)
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "44px")
                .set("height", "44px")
                .set("flex-shrink", "0");

        Span number = new Span(String.valueOf(value));
        number.getStyle().set("font-size", "var(--lumo-font-size-xxl)").set("font-weight", "600");
        Span labelEl = new Span(label);
        labelEl.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");
        VerticalLayout text = new VerticalLayout(number, labelEl);
        text.setPadding(false);
        text.setSpacing(false);

        HorizontalLayout card = new HorizontalLayout(iconBox, text);
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-m)");
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
