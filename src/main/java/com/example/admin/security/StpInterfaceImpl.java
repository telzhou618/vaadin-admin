package com.example.admin.security;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.convert.Convert;
import com.example.admin.system.entity.SysRole;
import com.example.admin.system.mapper.SysMenuMapper;
import com.example.admin.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Sa-Token 权限数据源：从数据库读取用户的角色与权限标识 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 拥有 admin 角色的用户放行全部权限
        if (getRoleList(loginId, loginType).contains("admin")) {
            return List.of("*");
        }
        return menuMapper.selectPermsByUserId(Convert.toLong(loginId));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return roleMapper.selectByUserId(Convert.toLong(loginId)).stream()
                .map(SysRole::getCode)
                .toList();
    }
}
