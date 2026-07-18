package com.example.admin.ui;

import cn.dev33.satoken.stp.StpUtil;
import com.example.admin.system.service.SysUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import lombok.Data;

/** 修改当前登录用户密码的弹窗 */
public class ChangePasswordDialog extends Dialog {

    @Data
    private static class PasswordForm {
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;
    }

    public ChangePasswordDialog(SysUserService userService) {
        setHeaderTitle("修改密码");
        setWidth("400px");

        PasswordField oldPassword = new PasswordField("原密码");
        PasswordField newPassword = new PasswordField("新密码");
        PasswordField confirmPassword = new PasswordField("确认新密码");
        oldPassword.setRequiredIndicatorVisible(true);
        newPassword.setRequiredIndicatorVisible(true);
        confirmPassword.setRequiredIndicatorVisible(true);

        // Binder 绑定与校验：校验失败时错误信息红色显示在字段下方
        Binder<PasswordForm> binder = new Binder<>(PasswordForm.class);
        binder.forField(oldPassword)
                .asRequired("请输入原密码")
                .bind(PasswordForm::getOldPassword, PasswordForm::setOldPassword);
        binder.forField(newPassword)
                .asRequired("请输入新密码")
                .withValidator(p -> p.length() >= 6 && p.length() <= 20, "密码长度需为 6-20 位")
                .bind(PasswordForm::getNewPassword, PasswordForm::setNewPassword);
        binder.forField(confirmPassword)
                .asRequired("请再次输入新密码")
                .withValidator(c -> c.equals(newPassword.getValue()), "两次输入的密码不一致")
                .bind(PasswordForm::getConfirmPassword, PasswordForm::setConfirmPassword);
        PasswordForm form = new PasswordForm();
        binder.readBean(form);

        FormLayout layout = new FormLayout(oldPassword, newPassword, confirmPassword);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        add(layout);

        Button cancel = new Button("取消", e -> close());
        Button save = new Button("保存", e -> {
            if (!binder.writeBeanIfValid(form)) {
                return;
            }
            try {
                userService.changePassword(StpUtil.getLoginIdAsLong(),
                        form.getOldPassword(), form.getNewPassword());
                close();
                Notification.show("密码修改成功，请重新登录", 3000, Notification.Position.TOP_CENTER);
                StpUtil.logout();
                getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            } catch (Exception ex) {
                oldPassword.setInvalid(true);
                oldPassword.setErrorMessage(ex.getMessage());
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
    }
}
