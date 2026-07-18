package com.example.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.system.entity.SysMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 查询用户可见的菜单（经用户-角色-菜单关联） */
    @Select("""
            select distinct m.* from sys_menu m
            join sys_role_menu rm on m.id = rm.menu_id
            join sys_user_role ur on rm.role_id = ur.role_id
            where ur.user_id = #{userId} and m.deleted = 0 and m.status = 0
            order by m.sort
            """)
    List<SysMenu> selectByUserId(Long userId);

    /** 查询用户拥有的权限标识 */
    @Select("""
            select distinct m.perms from sys_menu m
            join sys_role_menu rm on m.id = rm.menu_id
            join sys_user_role ur on rm.role_id = ur.role_id
            where ur.user_id = #{userId} and m.deleted = 0 and m.perms is not null and m.perms != ''
            """)
    List<String> selectPermsByUserId(Long userId);
}
