package com.github.pengpan.interceptor;

import java.io.IOException;
import java.util.List;

import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.cookie.CookieStore;
import com.github.pengpan.common.cookie.DefaultCookieConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
/**
 * @author pengpan
 */
@Slf4j
public class MainClientInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        
        Request.Builder requestBuilder = request.newBuilder();
        
        // Add cookies for 91160.com domains
        HttpUrl url = request.url();
        log.info("Request URL: {}", url.toString());
        boolean isSubmitRequest = url.toString().contains("ysubmit");
        log.debug("Is submit request: {}", isSubmitRequest);
        
        if (url.host().contains("91160.com")) {
            StringBuilder cookieHeader = new StringBuilder();
            
            // Get cookies from both user.91160.com and www.91160.com
            List<Cookie> userCookies = CookieStore.get("user.91160.com");
            List<Cookie> wwwCookies = CookieStore.get("www.91160.com");
            
            // Debug: Print available cookies
            log.info("=== Cookie Info ===");
            log.info("Request URL: {}", url.toString());
            log.info("User cookies count: {}", userCookies != null ? userCookies.size() : 0);
            log.info("WWW cookies count: {}", wwwCookies != null ? wwwCookies.size() : 0);
            
            if (userCookies != null && !userCookies.isEmpty()) {
                log.info("User cookies:");
                for (Cookie cookie : userCookies) {
                    log.info("  {}={} (expires: {})", cookie.name(), cookie.value(), cookie.expiresAt());
                }
            }
            
            if (wwwCookies != null && !wwwCookies.isEmpty()) {
                log.info("WWW cookies:");
                for (Cookie cookie : wwwCookies) {
                    log.info("  {}={} (expires: {})", cookie.name(), cookie.value(), cookie.expiresAt());
                }
            }
            
            // Add user.91160.com cookies first (authentication cookies)
            if (userCookies != null && !userCookies.isEmpty()) {
                for (int i = 0; i < userCookies.size(); i++) {
                    if (cookieHeader.length() > 0) {
                        cookieHeader.append("; ");
                    }
                    Cookie cookie = userCookies.get(i);
                    cookieHeader.append(cookie.name()).append("=").append(cookie.value());
                }
            }
            
            // Add www.91160.com cookies (session cookies)
            if (wwwCookies != null && !wwwCookies.isEmpty()) {
                for (int i = 0; i < wwwCookies.size(); i++) {
                    if (cookieHeader.length() > 0) {
                        cookieHeader.append("; ");
                    }
                    Cookie cookie = wwwCookies.get(i);
                    cookieHeader.append(cookie.name()).append("=").append(cookie.value());
                }
            }

            // 只有在提交请求时才使用默认Cookie补全
            if (isSubmitRequest) {
                log.info("提交请求Cookie数量不足，使用默认Cookie补全");
                
                // 如果user.91160.com的Cookie为空，添加默认Cookie
                if (userCookies == null || userCookies.isEmpty()) {
                    //TODO 应该从用户配置中获取
                    List<Cookie> defaultUserCookies = DefaultCookieConfig.getDefaultUserCookies();
                    CookieStore.put("user.91160.com", defaultUserCookies);
                    log.info("已添加默认user.91160.com Cookie: {} 个", defaultUserCookies.size());
                    
                    // 添加到请求头
                    for (Cookie cookie : defaultUserCookies) {
                        if (cookieHeader.length() > 0) {
                            cookieHeader.append("; ");
                        }
                        cookieHeader.append(cookie.name()).append("=").append(cookie.value());
                    }
                }
                
                // 如果www.91160.com的Cookie为空，添加默认Cookie
                if (wwwCookies == null || wwwCookies.isEmpty()) {
                    List<Cookie> defaultWwwCookies = DefaultCookieConfig.getDefaultWwwCookies();
                    CookieStore.put("www.91160.com", defaultWwwCookies);
                    log.info("已添加默认www.91160.com Cookie: {} 个", defaultWwwCookies.size());
                    
                    // 添加到请求头
                    for (Cookie cookie : defaultWwwCookies) {
                        if (cookieHeader.length() > 0) {
                            cookieHeader.append("; ");
                        }
                        cookieHeader.append(cookie.name()).append("=").append(cookie.value());
                    }
                }
            }
            
            if (cookieHeader.length() > 0) {
                log.debug("Final cookie header: {}", cookieHeader.toString());
                // //mock it 
                // String mockCookieHeader = "";
                requestBuilder.addHeader("Cookie", cookieHeader.toString());
            } else {
                log.warn("No cookies found for domain: {}", url.host());
            }
        }
        
        // 为提交请求设置请求头
        if (isSubmitRequest) {
            // 从请求体中提取参数来构建正确的 Referer URL
            String unitId = "131";
            String depId = "369"; 
            String scheduleId = "68e9933a283d48ea65e075c5";
            
            okhttp3.RequestBody requestBody = request.body();
            if (requestBody != null) {
                try {
                    okio.Buffer buffer = new okio.Buffer();
                    requestBody.writeTo(buffer);
                    String bodyString = buffer.readUtf8();
                    
                    // 从请求体中提取参数
                    unitId = extractParamFromBody(bodyString, "unit_id");
                    depId = extractParamFromBody(bodyString, "dep_id");
                    scheduleId = extractParamFromBody(bodyString, "schedule_id");

                    // 重新创建请求体，因为我们已经读取了它
                    requestBuilder.method(request.method(), 
                        okhttp3.RequestBody.create(requestBody.contentType(), bodyString));
                } catch (Exception e) {
                    log.warn("Failed to extract params from request body: {}", e.getMessage());
                }
            }
            
            // 构建正确的 Referer URL
            String refererUrl = String.format("%s/guahao/ystep1/uid-%s/depid-%s/schid-%s.html", 
                SystemConstant.DOMAIN, unitId, depId, scheduleId);
            log.info("Referer URL: {}", refererUrl);
            requestBuilder.addHeader("User-Agent", SystemConstant.DEFECT_USER_AGENT)
            //TODO 对于通道1和通道2，这里的referer是否兼容?
                    .addHeader("Referer", refererUrl)
                    .addHeader("Origin", SystemConstant.DOMAIN)
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Pragma", "no-cache")
                    .addHeader("Cache-Control", "no-cache");
                    // .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    // .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                    // .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    // .addHeader("Cache-Control", "no-cache")
                    // .addHeader("Pragma", "no-cache")
                    // .addHeader("Sec-Ch-Ua", "\"Chromium\";v=\"140\", \"Not=A?Brand\";v=\"24\", \"Google Chrome\";v=\"140\"")
                    // .addHeader("Sec-Ch-Ua-Mobile", "0")
                    // .addHeader("Sec-Ch-Ua-Platform", "\"Windows\"")
                    // .addHeader("Sec-Fetch-Dest", "document")
                    // .addHeader("Sec-Fetch-Mode", "navigate")
                    // .addHeader("Sec-Fetch-Site", "same-origin")
                    // .addHeader("Sec-Fetch-User", "1")
                    // .addHeader("Upgrade-Insecure-Requests", "1")
                    // .addHeader("Priority", "u=0, i");
        } else {
            requestBuilder.addHeader("User-Agent", SystemConstant.DEFECT_USER_AGENT)
                    .addHeader("Referer", SystemConstant.DOMAIN)
                    .addHeader("Origin", SystemConstant.DOMAIN)
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Pragma", "no-cache")
                    .addHeader("Cache-Control", "no-cache");
        }
        Request newReq = requestBuilder.build();
        return chain.proceed(newReq);
    }
    
    /**
     * 从请求体中提取参数值
     */
    private String extractParamFromBody(String bodyString, String paramName) {
        try {
            String[] pairs = bodyString.split("&");
            for (String pair : pairs) {
                if (pair.startsWith(paramName + "=")) {
                    return pair.substring((paramName + "=").length());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract param {} from body: {}", paramName, e.getMessage());
        }
        return ""; // 如果找不到参数，返回空字符串
    }
}
