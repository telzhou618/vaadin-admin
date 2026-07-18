package com.example.admin.config;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** 登录图形验证码：注册 /captcha 图片接口，验证码文本存入 Session */
@Configuration
public class CaptchaConfig {

    @Bean
    public ServletRegistrationBean<CaptchaServlet> captchaServlet() {
        return new ServletRegistrationBean<>(new CaptchaServlet(), "/captcha");
    }

    public static class CaptchaServlet extends HttpServlet {

        /** Session 中验证码的 key */
        public static final String SESSION_KEY = "LOGIN_CAPTCHA_CODE";

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 30);
            req.getSession().setAttribute(SESSION_KEY, captcha.getCode());
            resp.setContentType("image/png");
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setDateHeader("Expires", 0);
            ImageIO.write((BufferedImage) captcha.getImage(), "png", resp.getOutputStream());
        }
    }
}
