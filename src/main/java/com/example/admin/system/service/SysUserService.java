package com.example.admin.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.log.OperLog;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.entity.SysUserRole;
import com.example.admin.system.mapper.SysUserMapper;
import com.example.admin.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserRoleMapper userRoleMapper;

    /** 按关键字（用户名/昵称）查询用户列表 */
    public List<SysUser> listUsers(String keyword) {
        return lambdaQuery()
                .and(StrUtil.isNotBlank(keyword), q -> q
                        .like(SysUser::getUsername, keyword).or().like(SysUser::getNickname, keyword))
                .orderByDesc(SysUser::getCreateTime)
                .list();
    }

    /** 新增或更新用户，并重置其角色。密码留空表示不修改 */
    @OperLog("保存用户")
    @Transactional
    public void saveUser(SysUser user, List<Long> roleIds) {
        long sameName = lambdaQuery()
                .eq(SysUser::getUsername, user.getUsername())
                .ne(user.getId() != null, SysUser::getId, user.getId())
                .count();
        if (sameName > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            // null 字段 updateById 时忽略，即保持原密码不变
            user.setPassword(null);
        }
        saveOrUpdate(user);

        // 用户-角色关联：先删后插
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, user.getId()));
        if (CollUtil.isNotEmpty(roleIds)) {
            roleIds.forEach(roleId -> userRoleMapper.insert(new SysUserRole(user.getId(), roleId)));
        }
    }

    /** 用户已分配的角色 id 列表 */
    public List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }

    /** 删除用户及其角色关联 */
    @OperLog("删除用户")
    @Transactional
    public void deleteUser(Long userId) {
        removeById(userId);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
    }

    /** 用户自主修改密码：校验原密码后更新 */
    @OperLog("修改密码")
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        updateById(user);
    }
}
