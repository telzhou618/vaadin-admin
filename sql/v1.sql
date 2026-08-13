alter table sys_oper_log
    add column params text null comment '请求参数' after `operation`;