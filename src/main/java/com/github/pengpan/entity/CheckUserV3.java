package com.github.pengpan.entity;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author wufei
 */
@Data
public class CheckUserV3 {

    private String access_token;

    private String avatar;

    private String cat_id;

    private String city_id;

    private String email;

    private String fid;

    private String health_card;

    private String id;

    private String name;

    private String nickname;

    private String phone;

    private String sex;

    private String userId;

    private String user_age;

    private String user_id;

    private String user_ip;

    private String user_sex;

    /**
     * 创建带有默认值的 CheckUserV3 实例
     * @return CheckUserV3 实例
     */
    public static CheckUserV3 createDefault() {
        //TODO 应该从用户配置中获取
        CheckUserV3 checkUserV3 = new CheckUserV3();
        checkUserV3.setAccess_token("111");
        checkUserV3.setAvatar("https://wximg.91160.com/wechat/img/avatar/5.png");
        checkUserV3.setCat_id("");
        checkUserV3.setCity_id("");
        checkUserV3.setEmail("");
        checkUserV3.setFid("296013981");
        checkUserV3.setHealth_card("");
        checkUserV3.setId("");
        checkUserV3.setName("1");
        checkUserV3.setNickname("1");
        checkUserV3.setPhone("123456789");
        checkUserV3.setSex("0");
        checkUserV3.setUserId("");
        checkUserV3.setUser_age("");
        checkUserV3.setUser_id("");
        checkUserV3.setUser_ip("163.125.146.76");
        checkUserV3.setUser_sex("");
        return checkUserV3;
    }

    /**
     * 将 CheckUserV3 对象转换为 Map
     * @return Map<String, String>
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", this.access_token);
        map.put("avatar", this.avatar);
        map.put("cat_id", this.cat_id);
        map.put("city_id", this.city_id);
        map.put("email", this.email);
        map.put("fid", this.fid);
        map.put("health_card", this.health_card);
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("nickname", this.nickname);
        map.put("phone", this.phone);
        map.put("sex", this.sex);
        map.put("userId", this.userId);
        map.put("user_age", this.user_age);
        map.put("user_id", this.user_id);
        map.put("user_ip", this.user_ip);
        map.put("user_sex", this.user_sex);
        return map;
    }
}
