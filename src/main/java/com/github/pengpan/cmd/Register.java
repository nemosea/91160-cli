package com.github.pengpan.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.setting.dialect.Props;
import com.github.pengpan.common.constant.SystemConstant;
import com.github.pengpan.common.store.ConfigStore;
import com.github.pengpan.common.store.ProxyStore;
import com.github.pengpan.entity.Config;
import com.github.pengpan.enums.OcrPlatformEnum;
import com.github.pengpan.service.CaptchaService;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.service.DdddOcrService;
import com.github.pengpan.service.LoginService;
import com.github.pengpan.util.Assert;
import com.github.pengpan.util.CommonUtil;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "register", description = "Register on 91160.com")
public class Register implements Runnable {

    @Option(
            name = {"-c", "--config"},
            title = "configuration file",
            required = true,
            description = "Path to properties configuration file.")
    private String configFile;

    @Override
    public void run() {
        Config config = getConfig(configFile);

        CoreService coreService = SpringUtil.getBean(CoreService.class);

        checkBasicConfig(config);
        checkEnableProxy(config);

        if (config.isEnableAppoint()) {
            Assert.notBlank(config.getAppointTime(), "[appointTime]不能为空");
            Date appointTime = CommonUtil.parseDate(config.getAppointTime(), DatePattern.NORM_DATETIME_PATTERN);
            Assert.notNull(appointTime, "[appointTime]格式不正确，请检查配置文件");

            Date serverDate = coreService.serverDate();
            log.info("当前服务器时间: {}", DateUtil.formatDateTime(serverDate));
            log.info("指定的挂号时间: {}", DateUtil.formatDateTime(appointTime));

            long waitTime = appointTime.getTime() - serverDate.getTime();
            waitTime = waitTime < 0 ? 0 : waitTime;
            log.info("需等待: {}秒", TimeUnit.MILLISECONDS.toSeconds(waitTime));

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    log.info("时间到！！！");
                    doTask(coreService, config);
                }
            };
            timer.schedule(task, waitTime);

            log.info("等待中...");
        } else {
            doTask(coreService, config);
        }
    }

    private static void doTask(CoreService coreService, Config config) {
        try {
            coreService.brushTicketTask(config);
            CommonUtil.normalExit();
        } catch (Exception e) {
            CommonUtil.errorExit("", e);
        }
    }

    private void checkEnableProxy(Config config) {
        if (!config.isEnableProxy()) {
            return;
        }

        log.info("代理检测中...");

        String proxyFilePath = config.getProxyFilePath();

        Assert.notBlank(proxyFilePath, "[proxyFilePath]不能为空");
        Assert.isTrue(StrUtil.endWithIgnoreCase(proxyFilePath, "txt"), "[proxyFilePath]格式不正确，请检查配置文件");
        Assert.isTrue(FileUtil.exist(proxyFilePath), "[proxyFilePath]文件不存在，请检查配置文件");

        List<String> proxyList = CollUtil.newArrayList();

        List<String> lines = FileUtil.readUtf8Lines(proxyFilePath);
        for (String line : lines) {
            if (StrUtil.isEmpty(line)) {
                continue;
            }
            Matcher matcher = SystemConstant.PROXY_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            Proxy proxy = CommonUtil.getProxy(line);
            boolean proxyValid = CommonUtil.validateProxy(proxy);
            if (!proxyValid) {
                log.error("[{}]代理无效，已剔除", line);
                continue;
            }
            proxyList.add(line);
        }

        Assert.notEmpty(proxyList, "[proxyFilePath]至少要有一个可用的代理项");
        Assert.notNull(config.getProxyMode(), "[proxyMode]格式不正确，请检查配置文件");

        log.info("代理检测完成");

        ProxyStore.setProxyList(proxyList);
        ProxyStore.setEnabled(true);
        ProxyStore.setProxyMode(config.getProxyMode());
    }

    private Config getConfig(String configFile) {
        Assert.notBlank(configFile, "请指定配置文件");
        Assert.isTrue(configFile.endsWith(Props.EXT_NAME), "配置文件不正确");
        File file = new File(configFile);
        Assert.isTrue(file.exists(), "配置文件不存在，请检查文件路径");
        Props props = new Props(file, CharsetUtil.CHARSET_UTF_8);
        Config config = new Config();
        props.fillBean(config, null);
        return config;
    }

    private void checkBasicConfig(Config config) {
        CoreService coreService = SpringUtil.getBean(CoreService.class);
        LoginService loginService = SpringUtil.getBean(LoginService.class);
        CaptchaService captchaService = SpringUtil.getBean(CaptchaService.class);
        DdddOcrService ddddOcrService = SpringUtil.getBean(DdddOcrService.class);

        OcrPlatformEnum ocrPlatform = config.getOcrPlatform() == null ? OcrPlatformEnum.FATEADM : config.getOcrPlatform();
        Assert.notNull(ocrPlatform, "[ocrPlatform]不能为空，请检查配置文件");
        ConfigStore.setOcrPlatform(ocrPlatform.getId());
        if (ocrPlatform == OcrPlatformEnum.FATEADM) {
            Assert.notBlank(config.getPdId(), "[pdId]不能为空，请检查配置文件");
            Assert.notBlank(config.getPdKey(), "[pdKey]不能为空，请检查配置文件");
            Assert.isTrue(captchaService.pdCheck(config.getPdId(), config.getPdKey()), "PD账号验证失败，请检查PD账号和PD密钥");
        }
        if (ocrPlatform == OcrPlatformEnum.DDDDOCR) {
            Assert.notBlank(config.getBaseUrl(), "[baseUrl]不能为空，请检查配置文件");
            Assert.isTrue(ddddOcrService.baseUrlCheck(config.getBaseUrl()), "[baseUrl]验证失败，请检查配置文件");
        }
        Assert.notBlank(config.getUserName(), "[userName]不能为空，请检查配置文件");
        Assert.notBlank(config.getPassword(), "[password]不能为空，请检查配置文件");
        Assert.isTrue(loginService.doLoginRetry(config.getUserName(), config.getPassword(), SystemConstant.MAX_LOGIN_RETRY), "登录失败，请检查账号和密码");
        Assert.notBlank(config.getMemberId(), "[memberId]不能为空，请检查配置文件");

        Map<String, Object> selectMember = coreService.getMember().stream()
                .filter(x -> StrUtil.equals(String.valueOf(x.get("id")), config.getMemberId()))
                .findFirst().orElse(null);
        Assert.notNull(selectMember, "[memberId]不正确，请检查配置文件");
        //Assert.isTrue(BooleanUtil.toBoolean(String.valueOf(selectMember.get("certified"))), "[memberId]未认证，请检查配置文件");
        boolean certified = BooleanUtil.toBoolean(String.valueOf(selectMember.get("certified")));
        if (!certified) {
            log.warn("注意：[memberId]就诊人未认证，可能会导致挂号失败！");
        }

        Assert.notBlank(config.getCityId(), "[cityId]不能为空，请检查配置文件");
        Assert.notBlank(config.getUnitId(), "[unitId]不能为空，请检查配置文件");
        Assert.notBlank(config.getDeptId(), "[deptId]不能为空，请检查配置文件");
        Assert.notEmpty(config.getDoctorId(), "[doctorId]不能为空，请检查配置文件");
        Assert.notEmpty(config.getWeeks(), "[weeks]不能为空，请检查配置文件");
        Assert.notEmpty(config.getDays(), "[days]不能为空，请检查配置文件");
        Assert.isTrue(config.getRandomSleepTime() >= 0, "[sleepTime]格式不正确，请检查配置文件, 例如 \"sleepTime=3000-8000\" ");
        if (StrUtil.isNotBlank(config.getBrushStartDate())) {
            Date brushStartDate = CommonUtil.parseDate(config.getBrushStartDate(), DatePattern.NORM_DATE_PATTERN);
            Assert.notNull(brushStartDate, "[brushStartDate]格式不正确，请检查配置文件");
            Date today = DateUtil.beginOfDay(new Date());
            Assert.isTrue(brushStartDate.getTime() >= today.getTime(), "[brushStartDate]刷号起始日期不能小于当前日期");
        }
    }
}
