package com.example.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 内置管理员账号 id：不可删除、不可被强制下线 */
    public static final Long ADMIN_ID = 1L;

    private String username;

    private String password;

    private String nickname;

    private String email;

    /** 头像地址 */
    private String avatar;

    /** 手机号（11 位） */
    private String phone;

    /** 性别：0 男 1 女 2 保密 */
    private Integer gender;

    /** 生日 */
    private LocalDate birthday;

    /** 状态：0 正常 1 停用 */
    private Integer status;
}
