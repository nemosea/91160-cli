package com.github.pengpan.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author pengpan
 */
@Data
@Builder
public class Register {

    private String schData;
    private String memberId;
    private String addressId;
    private String address;
    private String diseaseInput;
    private String orderNo;
    private String diseaseContent;
    private String accept;
    private String unitId;
    private String schId;
    private String depId;
    private String hisDepId;
    private String schDate;
    private String timeType;
    private String doctorId;
    private String hisDocId;
    private String detlid;
    private String detlidRealtime;
    private String levelCode;
    private String isHot;
    private String payOnline;
    private String detlName;
    private String toDate;
    private String hisMemId;
}
