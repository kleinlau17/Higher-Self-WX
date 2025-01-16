package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.entity.Information;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.dto.InformationDTO;

/**
 * <p>
 * 用户运势信息表 服务类
 * </p>
 *
 * @author klein
 * @since 2025-01-11 06:34:15
 */
public interface InformationService extends IService<Information> {

    Information createInformation(InformationDTO dto);

    Information getInformationByOpenid(InformationDTO dto);

}
