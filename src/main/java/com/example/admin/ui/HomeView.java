package com.example.admin.ui;

import com.example.admin.security.AuthService;
import com.example.admin.system.entity.SysUser;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("首页 - vaadin-admin")
public class HomeView extends VerticalLayout {

    public HomeView(AuthService authService) {
        SysUser user = authService.getCurrentUser();
        String name = user.getNickname() == null ? user.getUsername() : user.getNickname();
        add(
                new H2("你好，" + name),
                new Paragraph("这是 vaadin-admin 管理后台模板，技术栈：Spring Boot + Vaadin + Sa-Token + MyBatis-Plus + MySQL。"),
                new Paragraph("通过左侧菜单进入用户、角色、菜单管理。")
        );
    }
}
