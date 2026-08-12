package com.example.admin.i18n;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;

import java.util.List;

/**
 * 中文化组件工具：统一创建中文文案的 DatePicker / Upload。
 */
public final class UiI18n {

    private UiI18n() {
    }

    /**
     * 中文日期选择器：日期格式保持 yyyy-MM-dd，按钮/月份/星期为中文。
     */
    public static DatePicker datePicker(String label) {
        DatePicker datePicker = new DatePicker(label);
        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n()
                .setToday("今天")
                .setCancel("取消")
                .setFirstDayOfWeek(1) // 周一为一周第一天（中国习惯）
                .setMonthNames(List.of("1月", "2月", "3月", "4月", "5月", "6月",
                        "7月", "8月", "9月", "10月", "11月", "12月"))
                .setWeekdays(List.of("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"))
                .setWeekdaysShort(List.of("日", "一", "二", "三", "四", "五", "六"));
        datePicker.setI18n(i18n);
        return datePicker;
    }

    /**
     * 给 Upload 应用中文文案。
     */
    public static void applyChinese(Upload upload) {
        UploadI18N i18n = new UploadI18N()
                .setDropFiles(new UploadI18N.DropFiles()
                        .setOne("拖放文件到此处")
                        .setMany("拖放多个文件到此处"))
                .setAddFiles(new UploadI18N.AddFiles()
                        .setOne("上传文件")
                        .setMany("上传多个文件"))
                .setUploading(new UploadI18N.Uploading()
                        .setStatus(new UploadI18N.Uploading.Status()
                                .setConnecting("正在连接...")
                                .setStalled("传输已暂停")
                                .setProcessing("正在处理...")
                                .setHeld("排队中..."))
                        .setRemainingTime(new UploadI18N.Uploading.RemainingTime()
                                .setPrefix("剩余时间 ")
                                .setUnknown("剩余时间未知"))
                        .setError(new UploadI18N.Uploading.Error()
                                .setServerUnavailable("服务器不可用")
                                .setUnexpectedServerError("服务器异常")
                                .setForbidden("禁止上传")))
                .setFile(new UploadI18N.File()
                        .setRemove("移除")
                        .setRetry("重试")
                        .setStart("开始"))
                .setError(new UploadI18N.Error()
                        .setTooManyFiles("文件数量超出限制")
                        .setFileIsTooBig("文件太大")
                        .setIncorrectFileType("文件类型不正确"));
        upload.setI18n(i18n);
    }
}
