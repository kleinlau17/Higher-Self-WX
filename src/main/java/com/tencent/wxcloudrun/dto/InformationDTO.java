package com.tencent.wxcloudrun.dto;

import lombok.Data;
import java.util.Date;
import java.sql.Time;

@Data
public class InformationDTO {
    private String openid;
    private Date birthDate;
    private Time birthTime;
    private String birthPlace;
}