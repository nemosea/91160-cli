package com.github.pengpan.service;

import java.awt.image.BufferedImage;

import com.github.pengpan.enums.LoginResultEnum;

/**
 * @author pengpan
 */
public interface LoginService {

    String getToken();

    @Deprecated
    boolean checkUser(String username, String password, String token);

    @Deprecated
    boolean login(String username, String password, String token);

    @Deprecated
    boolean doLogin(String username, String password);

    BufferedImage getCaptchaImage();

    boolean checkUserV2(String username, String password, String token, String code);

    LoginResultEnum loginV2(String username, String password, String token, String code);

    boolean doLoginV2(String username, String password);

    boolean doLoginV3(String username, String password);

    boolean doLogon(String username, String password);

    boolean doLoginRetry(String username, String password, int retries);

    boolean checkUserV3(String username, String password, String token, String code);
}
