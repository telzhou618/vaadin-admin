-- vaadin-admin 数据库初始化脚本（MySQL 8）
-- 默认账号：admin / 123456

create database if not exists vaadin_admin default character set utf8mb4 collate utf8mb4_general_ci;
use vaadin_admin;

-- ----------------------------
-- 用户表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
    id          bigint auto_increment primary key comment '主键',
    username    varchar(50)  not null comment '用户名',
    password    varchar(100) not null comment '密码（BCrypt 密文）',
    nickname    varchar(50)  null comment '昵称',
    email       varchar(100) null comment '邮箱',
    avatar      varchar(255) null comment '头像地址',
    phone       varchar(11)  null comment '手机号（11 位）',
    gender      tinyint      null comment '性别：0 男 1 女 2 保密',
    birthday    date         null comment '生日',
    status      tinyint      not null default 0 comment '状态：0 正常 1 停用',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    deleted     tinyint      not null default 0 comment '逻辑删除：0 正常 1 已删除'
) engine = innodb comment '用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
    id          bigint auto_increment primary key comment '主键',
    code        varchar(50)  not null comment '角色编码，如 admin',
    name        varchar(50)  not null comment '角色名称',
    description varchar(200) null comment '描述',
    status      tinyint      not null default 0 comment '状态：0 正常 1 停用',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    deleted     tinyint      not null default 0 comment '逻辑删除：0 正常 1 已删除'
) engine = innodb comment '角色表';

-- ----------------------------
-- 菜单表（目录/菜单/按钮）
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
    id          bigint auto_increment primary key comment '主键',
    parent_id   bigint       not null default 0 comment '上级菜单 id，根节点为 0',
    name        varchar(50)  not null comment '菜单名称',
    type        tinyint      not null default 1 comment '类型：0 目录 1 菜单 2 按钮',
    path        varchar(100) null comment '路由地址，如 system/user',
    icon        varchar(50)  null comment 'VaadinIcon 枚举名，如 cogs',
    perms       varchar(100) null comment '权限标识，如 sys:user',
    sort        int          not null default 0 comment '排序',
    status      tinyint      not null default 0 comment '状态：0 正常 1 停用',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    deleted     tinyint      not null default 0 comment '逻辑删除：0 正常 1 已删除'
) engine = innodb comment '菜单表';

-- ----------------------------
-- 用户-角色关联表
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
    id      bigint auto_increment primary key comment '主键',
    user_id bigint not null comment '用户 id',
    role_id bigint not null comment '角色 id',
    key idx_user_id (user_id),
    key idx_role_id (role_id)
) engine = innodb comment '用户-角色关联表';

-- ----------------------------
-- 角色-菜单关联表
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
    id      bigint auto_increment primary key comment '主键',
    role_id bigint not null comment '角色 id',
    menu_id bigint not null comment '菜单 id',
    key idx_role_id (role_id),
    key idx_menu_id (menu_id)
) engine = innodb comment '角色-菜单关联表';

-- ----------------------------
-- 操作日志表
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
    id          bigint auto_increment primary key comment '主键',
    username    varchar(50)  null comment '操作人',
    operation   varchar(100) null comment '操作描述',
    ip          varchar(50)  null comment '操作 IP',
    status      tinyint      not null default 0 comment '状态：0 成功 1 失败',
    error_msg   varchar(500) null comment '错误信息',
    cost_ms     bigint       null comment '耗时（毫秒）',
    create_time datetime     null comment '操作时间',
    update_time datetime     null comment '更新时间',
    deleted     tinyint      not null default 0 comment '逻辑删除：0 正常 1 已删除'
) engine = innodb comment '操作日志表';

-- ----------------------------
-- 种子数据
-- ----------------------------
-- 超级管理员，密码 123456（BCrypt）
insert into sys_user (id, username, password, nickname, email, phone, gender, birthday, status, create_time, update_time)
values (1, 'admin', '$2a$10$jD/M7YaW.fN5Wd3k6tR8gu6uhMZ.qvgr6tW5OSta5mYzdAVK2bSqS', '超级管理员', 'admin@example.com', '13888888888', 0, '1990-01-01', 0, now(), now());

-- 管理员角色（code=admin 的角色在代码中放行全部权限）
insert into sys_role (id, code, name, description, status, create_time, update_time)
values (1, 'admin', '管理员', '系统管理员，拥有全部权限', 0, now(), now());

-- 菜单树
insert into sys_menu (id, parent_id, name, type, path, icon, perms, sort, status, create_time, update_time) values
(1, 0, '系统管理', 0, null,         'cogs',      null,       1, 0, now(), now()),
(2, 1, '用户管理', 1, 'system/user', 'user',      'sys:user', 1, 0, now(), now()),
(3, 1, '角色管理', 1, 'system/role', 'key',       'sys:role', 2, 0, now(), now()),
(4, 1, '菜单管理', 1, 'system/menu', 'list',      'sys:menu', 3, 0, now(), now()),
(5, 1, '操作日志', 1, 'system/log',  'file',    'sys:log',  4, 0, now(), now());

-- admin 用户关联 admin 角色
insert into sys_user_role (user_id, role_id) values (1, 1);

-- admin 角色关联全部菜单
insert into sys_role_menu (role_id, menu_id) values (1, 1), (1, 2), (1, 3), (1, 4), (1, 5);


-- ----------------------------
-- 已有数据库升级（sys_user 新增头像/手机号/性别/生日字段时执行）
-- ----------------------------
-- alter table sys_user
--     add column avatar varchar(255) null comment '头像地址' after email,
--     add column phone varchar(11) null comment '手机号（11 位）' after avatar,
--     add column gender tinyint null comment '性别：0 男 1 女 2 保密' after phone,
--     add column birthday date null comment '生日' after gender;
