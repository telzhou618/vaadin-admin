package com.example.admin.security;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.example.admin.config.CaptchaConfig;
import com.example.admin.log.OperLog;
import com.example.admin.system.entity.SysMenu;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysMenuService;
import com.example.admin.system.service.SysUserService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 登录认证与当前用户信息 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService userService;
    private final SysMenuService menuService;

    /** 登录（先校验验证码），失败抛出 RuntimeException */
    @OperLog("登录")
    public void login(String username, String password, String captcha) {
        checkCaptcha(captcha);
        SysUser user = userService.lambdaQuery().eq(SysUser::getUsername, username).one();
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被停用，请联系管理员");
        }
        StpUtil.login(user.getId());
    }

    /** 校验图形验证码（一次性，取出后立即失效，防止重放） */
    private void checkCaptcha(String captcha) {
        WrappedSession session = VaadinSession.getCurrent().getSession();
        String expected = (String) session.getAttribute(CaptchaConfig.CaptchaServlet.SESSION_KEY);
        session.removeAttribute(CaptchaConfig.CaptchaServlet.SESSION_KEY);
        if (StrUtil.isBlank(captcha) || expected == null || !expected.equalsIgnoreCase(captcha.trim())) {
            throw new RuntimeException("验证码错误或已过期");
        }
    }

    /** 退出登录 */
    @OperLog("退出登录")
    public void logout() {
        StpUtil.logout();
    }

    /** 当前登录用户 */
    public SysUser getCurrentUser() {
        return userService.getById(StpUtil.getLoginIdAsLong());
    }

    /** 当前用户的菜单树（仅目录和菜单，用于侧边导航） */
    public List<SysMenu> getCurrentUserMenus() {
        List<SysMenu> menus = menuService.getBaseMapper().selectByUserId(StpUtil.getLoginIdAsLong())
                .stream()
                .filter(m -> m.getType() != null && m.getType() <= 1)
                .toList();
        return menuService.buildTree(menus);
    }
}
