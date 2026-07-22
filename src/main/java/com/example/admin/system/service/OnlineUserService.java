package com.example.admin.system.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.example.admin.log.OperLog;
import com.example.admin.system.dto.OnlineUser;
import com.example.admin.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 在线用户监控：列出全部登录会话，支持按 token 强制下线
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    /**
     * token session 中记录登录 IP 的 key
     */
    public static final String SESSION_IP = "loginIp";
    /**
     * token session 中记录登录时间的 key
     */
    public static final String SESSION_LOGIN_TIME = "loginTime";

    private final SysUserService userService;

    /**
     * 全部在线会话，按登录时间倒序
     */
    public List<OnlineUser> listOnline() {
        List<OnlineUser> result = new ArrayList<>();
        String currentToken = StpUtil.getTokenValue();
        // sa-token-redis-jackson 的 searchTokenValue 返回完整 key（satoken:login:token:xxx），需去掉前缀
        String prefix = StpUtil.stpLogic.splicingKeyTokenValue("");
        for (String key : StpUtil.searchTokenValue("", 0, -1, true)) {
            String token = key.startsWith(prefix) ? key.substring(prefix.length()) : key;
            Object loginId = StpUtil.getLoginIdByToken(token);
            long remain = StpUtil.getTokenTimeout(token);
            if (loginId == null || remain == -2) {
                continue;
            }
            SysUser user = userService.getById(Long.valueOf(String.valueOf(loginId)));
            if (user == null) {
                continue;
            }
            OnlineUser online = new OnlineUser();
            online.setToken(token);
            online.setUserId(user.getId());
            online.setUsername(user.getUsername());
            online.setNickname(user.getNickname());
            online.setRemainSeconds(remain);
            online.setCurrent(token.equals(currentToken));
            SaSession session = StpUtil.getTokenSessionByToken(token);
            if (session != null) {
                Object ip = session.get(SESSION_IP);
                online.setIp(ip == null ? null : String.valueOf(ip));
                if (session.get(SESSION_LOGIN_TIME) instanceof Number millis) {
                    online.setLoginTime(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(millis.longValue()), ZoneId.systemDefault()));
                }
            }
            result.add(online);
        }
        result.sort(Comparator.comparing(OnlineUser::getLoginTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * 按 token 强制下线。内置 admin 管理员与当前会话不可踢出
     */
    @OperLog("强制下线")
    public void kickout(String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            throw new RuntimeException("该会话已失效，请刷新列表");
        }
        if (SysUser.ADMIN_ID.equals(Long.valueOf(String.valueOf(loginId)))) {
            throw new RuntimeException("admin 管理员不能被强制下线");
        }
        if (token.equals(StpUtil.getTokenValue())) {
            throw new RuntimeException("不能强制下线自己");
        }
        StpUtil.kickoutByTokenValue(token);
    }
}
