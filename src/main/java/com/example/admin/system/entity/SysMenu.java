package com.example.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 上级菜单 id，根节点为 0 */
    private Long parentId;

    private String name;

    /** 类型：0 目录 1 菜单 2 按钮 */
    private Integer type;

    /** 路由地址，如 system/user */
    private String path;

    /** VaadinIcon 枚举名，如 cogs、user */
    private String icon;

    /** 权限标识，如 sys:user */
    private String perms;

    private Integer sort;

    /** 状态：0 正常 1 停用 */
    private Integer status;

    /** 子菜单（非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children;
}
