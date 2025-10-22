package com.github.pengpan.common.cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;

/**
 * 默认Cookie配置
 * 包含固定的Cookie值和动态生成的值
 * 
 * @author wufei
 */
@Slf4j
public class DefaultCookieConfig {
        //TODO 应该从用户配置中获取
    // 固定的Cookie值（从header.yaml中提取）
    private static final String FIXED_COOKIES = "";
    
    /**
     * 生成动态Cookie值
     */
    public static class DynamicCookies {
        
        /**
         * 生成DKLFFDKD Cookie（加密令牌）
         */
        public static String generateDKLFFDKD() {
            String key = RandomUtil.randomString(32);
            String val = RandomUtil.randomString(32);
            long tm = System.currentTimeMillis() / 1000;
            
            String json = String.format("{\"key\":\"%s\",\"val\":\"%s\",\"tm\":%d}", key, val, tm);
            //return java.net.URLEncoder.encode(json, java.nio.charset.StandardCharsets.UTF_8);
        try {
            return java.net.URLEncoder.encode(json, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        }
        
        /**
         * 生成FISKCDDCC Cookie（另一个加密令牌）
         */
        public static String generateFISKCDDCC() {
            return RandomUtil.randomString(32);
        }
        
        /**
         * 生成__guid Cookie（全局唯一标识符）
         */
        public static String generateGuid() {
            return RandomUtil.randomString(24) + "." + RandomUtil.randomInt(10000000, 99999999);
        }
        
        /**
         * 生成__jsluid_s Cookie（JavaScript库唯一ID）
         */
        public static String generateJsluidS() {
            return RandomUtil.randomString(32);
        }
        
        /**
         * 生成access_hash Cookie（访问哈希值）
         */
        public static String generateAccessHash() {
            String hash = RandomUtil.randomString(32);
            String suffix = RandomUtil.randomString(12) + System.currentTimeMillis();
            return hash + suffix;
        }
        
        /**
         * 生成PHPSESSID Cookie（PHP会话ID）
         */
        public static String generatePhpSessionId() {
            return RandomUtil.randomString(26);
        }
    }
    
    /**
     * 获取默认Cookie字符串
     * 包含固定Cookie和动态生成的Cookie
     */
    public static String getDefaultCookieString() {
        StringBuilder cookieBuilder = new StringBuilder();
        
        // 添加动态生成的Cookie
        cookieBuilder.append("DKLFFDKD=").append(DynamicCookies.generateDKLFFDKD()).append("; ");
        cookieBuilder.append("PHPSESSID=").append(DynamicCookies.generatePhpSessionId()).append("; ");
        cookieBuilder.append("__jsluid_s=").append(DynamicCookies.generateJsluidS()).append("; ");
        cookieBuilder.append("autoLoginInfo=0e9fe9374589fdb45bf40ad5f50e5e8f; ");
        cookieBuilder.append("__guid=").append(DynamicCookies.generateGuid()).append("; ");
        cookieBuilder.append("access_hash=").append(DynamicCookies.generateAccessHash()).append("; ");
        cookieBuilder.append("ip_city=sz; ");
        cookieBuilder.append("FISKCDDCC=").append(DynamicCookies.generateFISKCDDCC()).append("; ");
        
        // 添加固定的Cookie
        cookieBuilder.append(FIXED_COOKIES);
        
        return cookieBuilder.toString();
    }
    
    /**
     * 将Cookie字符串转换为Cookie对象列表
     */
    public static List<Cookie> parseCookieString(String cookieString, String domain) {
        List<Cookie> cookies = new ArrayList<>();
        
        if (StrUtil.isBlank(cookieString)) {
            return cookies;
        }
        
        String[] cookiePairs = cookieString.split(";");
        for (String pair : cookiePairs) {
            pair = pair.trim();
            if (StrUtil.isBlank(pair)) {
                continue;
            }
            
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String name = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                // 创建Cookie对象，设置较长的过期时间
                Cookie cookie = new Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain(domain)
                    .path("/")
                    .expiresAt(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30))
                    .build();
                
                cookies.add(cookie);
            }
        }
        
        return cookies;
    }
    
    /**
     * 获取www.91160.com的默认Cookie
     */
    public static List<Cookie> getDefaultWwwCookies() {
        String cookieString = getDefaultCookieString();
        return parseCookieString(cookieString, "www.91160.com");
    }
    
    /**
     * 获取user.91160.com的默认Cookie
     */
    public static List<Cookie> getDefaultUserCookies() {
        String cookieString = getDefaultCookieString();
        return parseCookieString(cookieString, "user.91160.com");
    }
}
