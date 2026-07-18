package com.example.admin.system.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.log.OperLog;
import com.example.admin.system.entity.SysRole;
import com.example.admin.system.entity.SysRoleMenu;
import com.example.admin.system.entity.SysUserRole;
import com.example.admin.system.mapper.SysRoleMapper;
import com.example.admin.system.mapper.SysRoleMenuMapper;
import com.example.admin.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    /** 新增或更新角色，并重置其菜单权限 */
    @OperLog("保存角色")
    @Transactional
    public void saveRole(SysRole role, List<Long> menuIds) {
        long sameCode = lambdaQuery()
                .eq(SysRole::getCode, role.getCode())
                .ne(role.getId() != null, SysRole::getId, role.getId())
                .count();
        if (sameCode > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        saveOrUpdate(role);

        // 角色-菜单关联：先删后插
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, role.getId()));
        if (CollUtil.isNotEmpty(menuIds)) {
            menuIds.forEach(menuId -> roleMenuMapper.insert(new SysRoleMenu(role.getId(), menuId)));
        }
    }

    /** 角色已分配的菜单 id 列表 */
    public List<Long> getMenuIds(Long roleId) {
        return roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    /** 删除角色及其菜单、用户关联 */
    @OperLog("删除角色")
    @Transactional
    public void deleteRole(Long roleId) {
        removeById(roleId);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, roleId));
    }
}
