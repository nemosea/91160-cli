package com.github.pengpan.common.constant;

import java.util.regex.Pattern;

/**
 * @author pengpan
 */
public class SystemConstant {

    public final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDWuY4Gff8FO3BAKetyvNgGrdZM9CMNoe45SzHMXxAPWw6E2idaEjqe5uJFjVx55JW+5LUSGO1H5MdTcgGEfh62ink/cNjRGJpR25iVDImJlLi2izNs9zrQukncnpj6NGjZu/2z7XXfJb4XBwlrmR823hpCumSD1WiMl1FMfbVorQIDAQAB";

    public final static String DEFECT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36";

    public final static String DOMAIN = "https://www.91160.com";

    public final static String HOST = "www.91160.com";

    public final static String FATEADM_DOMAIN = "http://pred.fateadm.com";

    public final static String CAPTCHA_COLLECT_URL = "http://captcha.dcode.top:8080/captchaCollect/save";

    public final static String LOGIN_URL = "https://user.91160.com/login.html";

    public final static String CHECK_USER_URL = "https://user.91160.com/checkUser.html";
    
    public final static String CHECK_USER_V3_URL = "https://www.91160.com/user/check.html";

    public final static String CAPTCHA_URL = "https://user.91160.com/Captcha.png";

    public final static Pattern PROXY_PATTERN = Pattern.compile("(socks|http)@(.*):(.*)");

    public final static int LIMIT_RETRIES = 100;

    public final static int MAX_LOGIN_RETRY = 5;

    public final static String DEFAULT_DDDD_OCR_BASE_URL = "http://127.0.0.1:8000";
}
