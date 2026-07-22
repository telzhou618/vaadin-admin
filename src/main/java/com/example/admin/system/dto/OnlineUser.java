package com.example.admin.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 在线用户会话信息（在线用户监控页面展示用） */
@Data
public class OnlineUser {

    /** 登录 token */
    private String token;

    private Long userId;

    private String username;

    private String nickname;

    /** 登录 IP */
    private String ip;

    /** 登录时间 */
    private LocalDateTime loginTime;

    /** 剩余有效期（秒），-1 表示永久 */
    private long remainSeconds;

    /** 是否当前浏览者的会话 */
    private boolean current;
}
