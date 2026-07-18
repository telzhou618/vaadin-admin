package com.example.admin.security;

import cn.dev33.satoken.stp.StpUtil;
import com.example.admin.ui.HomeView;
import com.example.admin.ui.LoginView;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Vaadin 路由守卫：未登录访问受限页面时跳转登录页，
 * 并按 @RequiresPerm 注解校验页面级权限。
 */
@Component
public class SecurityServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent ->
                uiEvent.getUI().addBeforeEnterListener(this::beforeEnter));
    }

    private void beforeEnter(BeforeEnterEvent event) {
        Class<?> target = event.getNavigationTarget();

        // 登录页：已登录则直接去首页
        if (LoginView.class.equals(target)) {
            if (StpUtil.isLogin()) {
                event.forwardTo(HomeView.class);
            }
            return;
        }

        // 其余页面：未登录跳转登录页
        if (!StpUtil.isLogin()) {
            event.rerouteTo(LoginView.class);
            return;
        }

        // 页面级权限校验
        RequiresPerm requiresPerm = target.getAnnotation(RequiresPerm.class);
        if (requiresPerm != null && !StpUtil.hasPermission(requiresPerm.value())) {
            Notification.show("没有访问权限", 3000, Notification.Position.MIDDLE);
            event.forwardTo(HomeView.class);
        }
    }
}
