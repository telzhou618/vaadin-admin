package com.example.admin.system.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.system.entity.SysOperLog;
import com.example.admin.system.mapper.SysOperLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysOperLogService extends ServiceImpl<SysOperLogMapper, SysOperLog> {

    /** 按关键字（操作人/操作描述）分页查询日志，最新在前 */
    public Page<SysOperLog> pageLogs(String keyword, int page, int size) {
        return lambdaQuery()
                .and(StrUtil.isNotBlank(keyword), q -> q
                        .like(SysOperLog::getUsername, keyword).or().like(SysOperLog::getOperation, keyword))
                .orderByDesc(SysOperLog::getId)
                .page(new Page<>(page, size));
    }

    /** 最近 N 条日志（首页 Dashboard 用） */
    public List<SysOperLog> listLatest(int limit) {
        return lambdaQuery().orderByDesc(SysOperLog::getId).last("limit " + limit).list();
    }
}
