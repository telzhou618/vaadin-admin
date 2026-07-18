package com.example.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.system.entity.SysRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 查询用户拥有的角色 */
    @Select("select r.* from sys_role r " +
            "join sys_user_role ur on r.id = ur.role_id " +
            "where ur.user_id = #{userId} and r.deleted = 0")
    List<SysRole> selectByUserId(Long userId);
}
