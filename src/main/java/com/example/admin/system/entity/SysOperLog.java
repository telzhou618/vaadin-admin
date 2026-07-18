package com.example.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
public class SysOperLog extends BaseEntity {

    /** 操作人用户名 */
    private String username;

    /** 操作描述，如 保存用户 */
    private String operation;

    /** 操作 IP */
    private String ip;

    /** 状态：0 成功 1 失败 */
    private Integer status;

    /** 失败时的错误信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costMs;
}
