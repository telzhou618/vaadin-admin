package com.example.admin.log;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.example.admin.system.entity.SysOperLog;
import com.example.admin.system.entity.SysUser;
import com.example.admin.system.service.SysOperLogService;
import com.example.admin.system.service.SysUserService;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** 操作日志切面：拦截 @OperLog 方法，记录操作人、执行结果与耗时 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final SysOperLogService operLogService;
    private final SysUserService userService;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint pjp, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperLog record = new SysOperLog();
        record.setUsername(currentUsername(pjp));
        record.setOperation(operLog.value());
        record.setIp(clientIp());
        try {
            Object result = pjp.proceed();
            record.setStatus(0);
            return result;
        } catch (Throwable e) {
            record.setStatus(1);
            record.setErrorMsg(errorMessage(e));
            throw e;
        } finally {
            record.setCostMs(System.currentTimeMillis() - start);
            saveQuietly(record);
        }
    }

    /** 操作人：已登录取当前用户名；未登录场景（如登录接口本身）取第一个字符串参数 */
    private String currentUsername(ProceedingJoinPoint pjp) {
        try {
            SysUser user = userService.getById(StpUtil.getLoginIdAsLong());
            if (user != null) {
                return user.getUsername();
            }
        } catch (Exception ignored) {
            // 未登录，走参数兜底
        }
        for (Object arg : pjp.getArgs()) {
            if (arg instanceof String s && StrUtil.isNotBlank(s)) {
                return s;
            }
        }
        return "anonymous";
    }

    private String clientIp() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        return request == null ? null : request.getRemoteAddr();
    }

    private String errorMessage(Throwable e) {
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }

    /** 日志写库失败不影响业务 */
    private void saveQuietly(SysOperLog record) {
        try {
            operLogService.save(record);
        } catch (Exception e) {
            log.error("操作日志写入失败", e);
        }
    }
}
