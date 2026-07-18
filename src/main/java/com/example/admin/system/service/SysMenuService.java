package com.example.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.log.OperLog;
import com.example.admin.system.entity.SysMenu;
import com.example.admin.system.entity.SysRoleMenu;
import com.example.admin.system.mapper.SysMenuMapper;
import com.example.admin.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    private final SysRoleMenuMapper roleMenuMapper;

    /** 全量菜单树（按 sort 升序） */
    public List<SysMenu> tree() {
        return buildTree(list(Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSort)));
    }

    /** 把平铺的菜单列表组装成树，返回根节点列表 */
    public List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() > 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        menus.forEach(m -> m.setChildren(childrenMap.getOrDefault(m.getId(), List.of())));
        return menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .toList();
    }

    /** 新增或更新菜单 */
    @OperLog("保存菜单")
    public void saveMenu(SysMenu menu) {
        saveOrUpdate(menu);
    }

    /** 删除菜单，有子菜单时不允许删除 */
    @OperLog("删除菜单")
    @Transactional
    public void deleteMenu(Long menuId) {
        long children = count(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, menuId));
        if (children > 0) {
            throw new RuntimeException("请先删除该菜单下的子菜单");
        }
        removeById(menuId);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, menuId));
    }
}
