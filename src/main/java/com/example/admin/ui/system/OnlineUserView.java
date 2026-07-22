package com.example.admin.ui.system;

import cn.hutool.core.date.DateUtil;
import com.example.admin.security.RequiresPerm;
import com.example.admin.system.dto.OnlineUser;
import com.example.admin.system.service.OnlineUserService;
import com.example.admin.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/** 在线用户监控：展示全部登录会话，可强制踢下线 */
@Route(value = "system/online", layout = MainLayout.class)
@PageTitle("在线用户 - vaadin-admin")
@RequiresPerm("sys:online")
public class OnlineUserView extends VerticalLayout {

    private final OnlineUserService onlineUserService;
    private final Grid<OnlineUser> grid = new Grid<>(OnlineUser.class, false);

    public OnlineUserView(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
        setSizeFull();

        H2 title = new H2("在线用户");
        title.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");

        Button reload = new Button("刷新", new Icon(VaadinIcon.REFRESH), e -> refresh());
        reload.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout toolbar = new HorizontalLayout(title, reload);
        toolbar.setWidthFull();
        toolbar.expand(title);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        grid.addColumn(OnlineUser::getUsername).setHeader("用户名");
        grid.addColumn(OnlineUser::getNickname).setHeader("昵称");
        grid.addComponentColumn(this::sessionBadge).setHeader("会话").setWidth("100px").setFlexGrow(0);
        grid.addColumn(OnlineUser::getIp).setHeader("登录 IP");
        grid.addColumn(u -> u.getLoginTime() == null ? "" : DateUtil.format(u.getLoginTime(), "yyyy-MM-dd HH:mm:ss"))
                .setHeader("登录时间");
        grid.addColumn(u -> formatRemain(u.getRemainSeconds())).setHeader("剩余有效期");
        grid.addComponentColumn(this::actionButtons).setHeader("操作").setWidth("120px").setFlexGrow(0);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);

        add(toolbar, grid);
        refresh();
    }

    /** 会话徽标：绿色当前会话 / 灰色其他在线会话 */
    private Component sessionBadge(OnlineUser user) {
        Span badge = new Span(user.isCurrent() ? "当前会话" : "在线");
        badge.getElement().getThemeList().add(user.isCurrent() ? "badge success" : "badge");
        return badge;
    }

    private Component actionButtons(OnlineUser user) {
        Button kick = new Button("强制下线", e -> confirmKick(user));
        kick.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        return kick;
    }

    private void confirmKick(OnlineUser user) {
        ConfirmDialog dialog = new ConfirmDialog("强制下线",
                "确定将用户「" + user.getUsername() + "」的该会话踢下线吗？", "踢下线", e -> {
            onlineUserService.kickout(user.getToken());
            refresh();
            Notification.show("已强制下线");
        });
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelable(true);
        dialog.setCancelText("取消");
        dialog.open();
    }

    /** 剩余有效期格式化：永久 / x 天 x 小时 / x 小时 x 分 / x 分 */
    private String formatRemain(long seconds) {
        if (seconds == -1) {
            return "永久";
        }
        if (seconds >= 86400) {
            return seconds / 86400 + " 天 " + seconds % 86400 / 3600 + " 小时";
        }
        if (seconds >= 3600) {
            return seconds / 3600 + " 小时 " + seconds % 3600 / 60 + " 分";
        }
        return seconds / 60 + " 分";
    }

    private void refresh() {
        grid.setItems(onlineUserService.listOnline());
    }
}
