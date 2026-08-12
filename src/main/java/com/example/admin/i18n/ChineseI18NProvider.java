package com.example.admin.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * 全局中文本地化：仅提供简体中文 locale，强制所有 UI 使用中文。
 */
@Component
public class ChineseI18NProvider implements I18NProvider {

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(Locale.SIMPLIFIED_CHINESE);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        return null;
    }
}
