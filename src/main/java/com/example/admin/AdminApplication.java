package com.example.admin;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.admin.system.mapper")
@CssImport("./styles/vaadin-admin.css")
public class AdminApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
