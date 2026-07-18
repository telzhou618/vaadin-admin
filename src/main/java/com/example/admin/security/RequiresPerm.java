package com.example.admin.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 标注在 Vaadin 视图类上，声明访问该页面所需的权限标识 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPerm {

    /** 权限标识，如 sys:user */
    String value();
}
