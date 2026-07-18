package com.example.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色编码，如 admin */
    private String code;

    private String name;

    private String description;

    /** 状态：0 正常 1 停用 */
    private Integer status;
}
