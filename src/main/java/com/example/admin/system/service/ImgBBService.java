package com.example.admin.system.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ImgBB 图床上传服务。
 */
@Slf4j
@Service
public class ImgBBService {

    @Value("${imgbb.api-url}")
    private String apiUrl;

    @Value("${imgbb.api-key}")
    private String apiKey;

    /**
     * 上传图片到 ImgBB
     */
    public String upload(byte[] imageBytes, String fileName) {
        HttpResponse response = HttpRequest.post(apiUrl)
                .timeout(30000)
                .form("key", apiKey)
                .form("image", cn.hutool.core.codec.Base64.encode(imageBytes))
                .form("name", StrUtil.isBlank(fileName) ? "avatar" : fileName)
                .execute();

        if (!response.isOk()) {
            return null;
        }
        JSONObject json = JSONUtil.parseObj(response.body());
        Boolean success = json.getBool("success");
        if (success == null || !success) {
            return null;
        }
        return json.getByPath("data.url", String.class);
    }
}
