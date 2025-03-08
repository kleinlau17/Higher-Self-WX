package com.tencent.wxcloudrun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

/**
 * <p>
 * 用户运势信息表
 * </p>
 *
 * @author klein
 * @since 2025-01-11 06:34:15
 */
@Getter
@Setter
@TableName("information")
public class Information implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 出生时间
     */
    private Time birthTime;

    /**
     * 出生地
     */
    private String birthPlace;

    /**
     * 性别
     */
    private String gender;

    /**
     * 紫微斗数
     */
    private String ziwei;

    /**
     * 生辰八字
     */
    private String bazi;

    /**
     * 运势年份
     */
    private Integer fortuneYear;

    /**
     * 事业性格
     */
    private String careerPersonality;

    /**
     * 流年大运
     */
    private String annualFortune;

    /**
     * 十二个月各个月的事业运和对应分数，JSON 格式
     */
    private String monthlyFortune;

    /**
     * 事业灵签
     */
    private String careerSignature;

    /**
     * 年度总结
     */
    private String annualSummary;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}


