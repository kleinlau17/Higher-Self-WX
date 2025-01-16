package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.entity.Information;
import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import com.tencent.wxcloudrun.service.InformationService;
import com.tencent.wxcloudrun.dto.InformationDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用户运势信息表 前端控制器
 * </p>
 * 
 * @author klein
 * @since 2025-01-11 06:34:15
 */
@RestController
@RequestMapping("/information")
@Slf4j
public class InformationController {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private InformationService informationService;

    public InformationController(ChatLanguageModel chatLanguageModel, InformationService informationService) {
        this.chatLanguageModel = chatLanguageModel;
        this.informationService = informationService;
    }

    @GetMapping("/generate")
    public String model(@RequestParam String message) {
        return chatLanguageModel.generate(message);
    }

    @GetMapping("/list")
    public Object list() {
        return informationService.list();
    }

    @PostMapping("/create")
    public Information createInformation(@RequestBody InformationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (dto.getOpenid() == null || dto.getOpenid().trim().isEmpty()) {
            throw new IllegalArgumentException("openid不能为空");
        }
        if (dto.getBirthDate() == null) {
            throw new IllegalArgumentException("出生日期不能为空");
        }
        if (dto.getBirthPlace() == null || dto.getBirthPlace().trim().isEmpty()) {
            throw new IllegalArgumentException("出生地点不能为空");
        }

        log.info("Received create information request for openid: {}", dto.getOpenid());
        Information result = informationService.createInformation(dto);
        return result;
    }

    @GetMapping("/getByOpenid")
    public Information getInformationByOpenid(@RequestParam String openid) {

        if (openid == null || openid.trim().isEmpty()) {
            throw new IllegalArgumentException("openid不能为空");
        }

        log.info("Querying information for openid: {}", openid);
        InformationDTO dto = new InformationDTO();
        dto.setOpenid(openid);
        return informationService.getInformationByOpenid(dto);
    }
}