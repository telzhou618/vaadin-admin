package com.example.admin.security;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.example.admin.system.entity.SysMenu;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysMenuService;
import com.example.admin.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 登录认证与当前用户信息 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService userService;
    private final SysMenuService menuService;

    /** 登录，失败抛出 RuntimeException */
    public void login(String username, String password) {
        SysUser user = userService.lambdaQuery().eq(SysUser::getUsername, username).one();
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("账号已被停用，请联系管理员");
        }
        StpUtil.login(user.getId());
    }

    /** 退出登录 */
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
