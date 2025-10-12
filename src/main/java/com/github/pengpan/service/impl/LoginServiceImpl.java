package com.github.pengpan.service.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.ejlchina.json.JSONKit;
import com.github.pengpan.client.MainClient;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.store.AccountStore;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.entity.CheckUser;
import com.github.pengpan.entity.CheckUserV3;
import com.github.pengpan.entity.fateadm.CapRegResult;
import com.github.pengpan.enums.LoginResultEnum;
import com.github.pengpan.enums.OcrPlatformEnum;
import com.github.pengpan.service.CaptchaService;
import com.github.pengpan.service.DdddOcrService;
import com.github.pengpan.service.LoginService;
import com.github.pengpan.util.Assert;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author pengpan
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    private static final RSA rsa = SecureUtil.rsa(null, SystemConstant.PUBLIC_KEY);

    @Resource
    private MainClient mainClient;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private DdddOcrService ddddOcrService;

    @Override
    public String getToken() {
        String html = mainClient.htmlPage(SystemConstant.LOGIN_URL);
        Document document = Jsoup.parse(html);
        Element tokens = document.getElementById("tokens");
        Assert.notNull(tokens, "token获取失败");
        String token = tokens.val();
        log.info("token: " + token);
        return token;
    }

    @Deprecated
    @Override
    public boolean checkUser(String username, String password, String token) {
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("type", "m");
        fields.put("token", token);

        CheckUser checkUser = mainClient.checkUser(SystemConstant.CHECK_USER_URL, fields);
        Assert.notNull(checkUser, "用户检测失败");
        if (StrUtil.equals("1", checkUser.getCode())) {
            log.info("用户检测通过");
            return true;
        } else {
            log.warn("用户检测不通过: " + checkUser.getMsg());
            return false;
        }
    }

    @Deprecated
    @Override
    public boolean login(String username, String password, String token) {
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("target", SystemConstant.DOMAIN);
        fields.put("error_num", "0");
        fields.put("token", token);

        Response<Void> loginResp = mainClient.doLogin(SystemConstant.LOGIN_URL, fields);
        if (!loginResp.raw().isRedirect()) {
            log.warn("登录失败，请检查用户名和密码");
            return false;
        }

        String redirectUrl = loginResp.headers().get("Location");
        Response<String> redirectResp = mainClient.loginRedirect(redirectUrl);
        boolean loginSuccess = redirectResp.raw().isRedirect();
        if (loginSuccess) {
            AccountStore.store(username, password);
            log.info("登录成功");
        } else {
            log.warn("登录失败，请先去网页端(https://user.91160.com/login.html)登录成功后再次尝试");
            log.error("Response code: {}", redirectResp.code());
            String errorBody = null;
            try {
                if (redirectResp.errorBody() != null) {
                    errorBody = redirectResp.errorBody().string();
                }
            } catch (IOException ignored) {
            }
            log.error("Response errorBody: {}", errorBody);
        }
        return loginSuccess;
    }

    @Deprecated
    @Override
    public boolean doLogin(String username, String password) {
        String token = getToken();
        return checkUser(username, password, token)
                && login(username, password, token);
    }

    @Override
    public BufferedImage getCaptchaImage() {
        Response<ResponseBody> captcha = mainClient.captcha(SystemConstant.CAPTCHA_URL);
        return ImgUtil.read(captcha.body().byteStream());
    }

    @Override
    public boolean checkUserV2(String username, String password, String token, String code) {
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("type", "m");
        fields.put("token", token);
        fields.put("checkcode", code);

        CheckUser checkUser = mainClient.checkUser(SystemConstant.CHECK_USER_URL, fields);
        Assert.notNull(checkUser, "用户检测失败");
        if (StrUtil.equals("1", checkUser.getCode())) {
            log.info("用户检测通过");
            return true;
        } else {
            log.warn("用户检测不通过: " + checkUser.getMsg());
            return false;
        }
    }

    @Override
    public LoginResultEnum loginV2(String username, String password, String token, String code) {
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        Map<String, String> fields = MapUtil.newHashMap();
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("target", SystemConstant.DOMAIN);
        fields.put("error_num", "0");
        fields.put("token", token);
        fields.put("checkcode", code);

        Response<String> loginResp = mainClient.doLoginV2(SystemConstant.LOGIN_URL, fields);
        if (!loginResp.raw().isRedirect()) {
            String body = loginResp.body();
            if (StrUtil.contains(body, "验证码有误或已失效")) {
                log.warn("登录失败，验证码有误或已失效");
                return LoginResultEnum.CAPTCHA_INCORRECT;
            }
            log.warn("登录失败，请检查用户名和密码");
            return LoginResultEnum.FAILED;
        }

        String redirectUrl = loginResp.headers().get("Location");
        Response<String> redirectResp = mainClient.loginRedirect(redirectUrl);
        boolean loginSuccess = redirectResp.raw().isRedirect();
        if (loginSuccess) {
            AccountStore.store(username, password);
            log.info("登录成功");
            return LoginResultEnum.SUCCESS;
        } else {
            log.warn("登录失败，请先去网页端(https://user.91160.com/login.html)登录成功后再次尝试");
            log.error("Response code: {}", redirectResp.code());
            String errorBody = null;
            try {
                if (redirectResp.errorBody() != null) {
                    errorBody = redirectResp.errorBody().string();
                }
            } catch (IOException ignored) {
            }
            log.error("Response errorBody: {}", errorBody);
            return LoginResultEnum.FAILED;
        }
    }

    @Override
    public boolean doLoginV2(String username, String password) {
        String token = getToken();

        BufferedImage captchaImage = getCaptchaImage();

        CapRegResult capRegResult = captchaService.capReg(captchaImage);
        String code = Optional.ofNullable(capRegResult)
                .map(CapRegResult::getResult)
                .orElseGet(String::new);

        // Skip checkUserV2 to bypass anti-bot detection
        log.info("Skipping checkUserV2 because it is duplicate");

        LoginResultEnum loginResult = loginV2(username, password, token, code);
        if (loginResult == LoginResultEnum.SUCCESS) {
            String captchaBase64 = ImgUtil.toBase64DataUri(captchaImage, "png");
            log.info("captcha: " + captchaBase64);
            log.info("code: " + code);
            // collect
            captchaCollect(captchaBase64, code);
            boolean checkUserv3 = checkUserV3(username, password, token, code);
            if (!checkUserv3) {
                return false;
            }
            return true;
        }
        if (loginResult == LoginResultEnum.CAPTCHA_INCORRECT) {
            String requestId = Optional.ofNullable(capRegResult)
                    .map(CapRegResult::getRequestId)
                    .orElseGet(String::new);
            captchaService.capJust(requestId);
            return false;
        }
        return false;
    }

    @Override
    public boolean doLoginV3(String username, String password) {
        String token = getToken();

        BufferedImage captchaImage = getCaptchaImage();
        String code = ddddOcrService.ocr(captchaImage);

        // Skip checkUserV2 to bypass anti-bot detection
        log.info("Skipping checkUserV2 because it is deprecated");
        
        LoginResultEnum loginResult = loginV2(username, password, token, code);
        if (loginResult == LoginResultEnum.SUCCESS) {
            String captchaBase64 = ImgUtil.toBase64DataUri(captchaImage, "png");
            log.info("captcha: " + captchaBase64);
            log.info("code: " + code);
            // collect
            captchaCollect(captchaBase64, code);

            boolean checkUserv3 = checkUserV3(username, password, token, code);
            if (!checkUserv3) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean doLogon(String username, String password) {
        OcrPlatformEnum ocrPlatform = OcrPlatformEnum.getById(ConfigStore.getOcrPlatform());

        if (ocrPlatform == OcrPlatformEnum.FATEADM) {
            return doLoginV2(username, password);
        }
        if (ocrPlatform == OcrPlatformEnum.DDDDOCR) {
            return doLoginV3(username, password);
        }

        return doLoginV2(username, password);
    }

    private void captchaCollect(String image, String code) {
        Map<String, String> body = MapUtil.<String, String>builder()
                .put("image", image)
                .put("code", code)
                .build();
        try {
            String result = HttpUtil.post(SystemConstant.CAPTCHA_COLLECT_URL, JSONKit.toJson(body), 5000);
            log.info("collect: " + result);
        } catch (Exception e) {
            log.error("collect error: " + e.getMessage());
        }
    }

    @Override
    public boolean doLoginRetry(String username, String password, int retries) {
        if (retries <= 0) {
            return false;
        }
        if (doLogon(username, password)) {
            return true;
        } else {
            log.warn("登录失败，剩余重试次数: " + (retries - 1));
            int sleepMs = RandomUtil.randomInt(1000, 3000);
            ThreadUtil.sleep(sleepMs, TimeUnit.MILLISECONDS);
            return doLoginRetry(username, password, retries - 1);
        }
    }

    @Override
    public boolean checkUserV3(String username, String password, String token, String code) {
        // Add human-like delay before checkUserV3
        try {
            Thread.sleep(RandomUtil.randomInt(1000, 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("checkUserV3");
        
        String encryptedUsername = Base64.encode(rsa.encrypt(username, KeyType.PublicKey));
        String encryptedPassword = Base64.encode(rsa.encrypt(password, KeyType.PublicKey));

        // 创建默认的 CheckUserV3 实例并转换为 Map
        CheckUserV3 defaultCheckUserV3 = CheckUserV3.createDefault();
        Map<String, String> fields = defaultCheckUserV3.toMap();
        
        // 添加登录相关的字段
        fields.put("username", encryptedUsername);
        fields.put("password", encryptedPassword);
        fields.put("type", "m");
        fields.put("token", token);
        fields.put("checkcode", code);

        CheckUserV3 checkUserV3 = mainClient.checkUserV3(SystemConstant.CHECK_USER_V3_URL, fields);
        Assert.notNull(checkUserV3, "用户检测V3失败");
        
        log.info("CheckUserV3 response: {}", checkUserV3);
        
        // 由于CheckUserV3没有code字段，我们检查是否有access_token返回来判断成功
        if (StrUtil.isNotBlank(checkUserV3.getAccess_token())) {
            log.info("用户检测V3通过");
            return true;
        } else {
            log.warn("用户检测V3不通过");
            return false;
        }
    }
}
