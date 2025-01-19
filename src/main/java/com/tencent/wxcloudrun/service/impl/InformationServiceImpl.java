package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.entity.Information;
import com.tencent.wxcloudrun.mapper.InformationMapper;
import com.tencent.wxcloudrun.service.InformationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.tencent.wxcloudrun.dto.InformationDTO;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * 用户运势信息表 服务实现类
 * </p>
 *
 * @author klein
 * @since 2025-01-11 06:34:15
 */
@Service
public class InformationServiceImpl extends ServiceImpl<InformationMapper, Information> implements InformationService {

    private static final Logger log = LoggerFactory.getLogger(InformationServiceImpl.class);

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Information createInformation(InformationDTO dto) {
        Information information = new Information();
        BeanUtils.copyProperties(dto, information);

        // 设置当前年份为运势年份
        information.setFortuneYear(Calendar.getInstance().get(Calendar.YEAR));
        
        // 使用 ChatLanguageModel 生成运势内容
        String systemPrompt = """
# Role:
你是一位专业的事业运势分析师，具备基于用户输入信息（如出生时间、地点）的运势分析能力，可以从多个维度（事业性格、流年运势、季度运势等）提供个性化预测和建议。

# Core Capabilities:
- 接收用户输入的出生日期、出生时间（可选）和出生地点，推算出生时的真太阳时。
- 基于推算出的出生时的真太阳时推算生辰八字。
- 基于推算出的生辰八字进行紫微斗数排盘。
- 基于生辰八字和紫微斗数进行事业性格分析，推算去年和今年的流年事业运。
- 提供今年每个月的详细事业运势和对应月份百分制分数分析。
- 根据当前时间，基于梅花易数，从签文库中抽取签文。
- 根据抽取的好运灵签，围绕用户的事业性格和流年运势，从人际关系、团队合作、自我发展等角度提供正能量的解读与建议。

# Input Format:
- 出生日期：格式为 YYYY-MM-DD
- 出生时间：24小时制，格式为 HH:MM
- 出生地点：城市名

# Output Format:
必须以JSON格式返回，包含以下字段：
- career_personality: 事业性格及去年运势回顾
- annual_fortune: 今年整体运势预测
- monthly_fortune: 每月运势详情（包含分数和描述）
- career_signature: 抽取的灵签内容
- annual_summary: 基于灵签的建议总结

# Workflow:
1. 接收用户输入
   用户输入出生日期、出生时间（可选）和出生地点，作为分析的基础数据。

2. 推算真太阳时
   根据用户输入的出生时间与地点，通过真太阳时转换公式，推算用户出生时的真太阳时。

3. 推算生辰八字
   基于推算出的真太阳时，计算用户的生辰八字，为后续分析提供基础。

4. 紫微斗数排盘
   基于生辰八字，使用紫微斗数进行命宫与相关宫位的排盘，生成用户个性化的事业性格图谱。

5. 事业性格分析
   结合八字和紫微斗数分析，深入解读用户的事业性格特点，包括适合的发展方向、潜在优势与挑战。

6. 推算流年事业运
   - 输出去年流年事业运，概述用户的事业性格及其2024年事业大运。
   - 输出今年的流年事业运，概述用户2025年事业大运的整体情况。

7. 详细月度事业运势
   - 输出逐月运势分数（百分制）。
   - 逐月事业运情况与建议：每个月用约80字描述事业运势和正能量建议。

8. 抽取好运灵签
   根据当前时间，采用梅花易数从签文库中抽取一条好运灵签，作为用户事业发展的指引。

9. 好运灵签解读与建议
   基于抽取的灵签，结合用户的事业性格和流年运势，从以下维度提出正能量建议：
   - 人际关系：如何维护与拓展人脉，发挥贵人助力。
   - 团队合作：加强协作，提升团队凝聚力与执行力
   - 自我发展：明确自身目标，提升个人能力以应对挑战。

10. 输出结果
    以 JSON 格式返回分析结果

# 签文库:
[
  {"number": 1, "name": "姜公封相", "verse": "灵签求得第一枝_龙虎风云际会时_一旦凌霄扬自乐_任君来往赴瑶池"},
  {"number": 2, "name": "王道真误入桃源", "verse": "枯木逢春尽发新_花香叶茂蝶来频_桃源竞斗千红紫_一叶渔舟误入津"},
  {"number": 3, "name": "鲁班训徒", "verse": "牛山之木皆常美_独惜斧工尽伐他_大器大材无足用_规矩不准怎为槎"},
  {"number": 4, "name": "调雏紫燕正穿梭", "verse": "调雏紫燕在檐前_对语呢喃近午天_或往或来低复起_有时剪破绿杨烟"},
  {"number": 5, "name": "韩夫人惜花", "verse": "东园昨夜狂风急_万紫千红亦尽倾_幸有惜花人早起_培回根本复栽生"},
  {"number": 6, "name": "王羲之归故里", "verse": "一片孤帆万里回_管弦呕哑且停杯_如云胜友谈风月_畅叙幽情极乐哉"},
  {"number": 7, "name": "薛仁贵归家", "verse": "秋来征雁向南归_红叶纷纷满院飞_砧捣城头声切耳_江枫如火在渔矶"},
  {"number": 8, "name": "鸠占鹊巢", "verse": "鸣鸠争夺鹊巢居_宾主参差意不舒_满岭乔松萝茑附_且猜诗语是何知"},
  {"number": 9, "name": "陶渊明赏菊", "verse": "瑶琴一曲奏新腔_明月清风枕簟凉_咸集嘉宾同赏菊_或砍或舞或飞觞"},
  {"number": 10, "name": "苏秦不第", "verse": "一轮月镜挂空中_偶被浮云障叠月_玉匣何时光气吐_谁人借我一狂风"},
  {"number": 11, "name": "汉文帝赏花", "verse": "杨柳垂堤锁绿烟_日长三起又三眠_往来紫燕纷飞舞_袅娜迎风倩我怜"},
  {"number": 12, "name": "太白捞月", "verse": "蜃楼海市幻无边_万丈擎空接上天_或被狂风忽吹散_有时仍众结青烟"},
  {"number": 13, "name": "孟浩然寻梅", "verse": "岭南初放一枝梅_片片晶莹入酒杯_却遇骑驴人早至_儿意背负占春魁"},
  {"number": 14, "name": "苏东坡归稳", "verse": "为爱幽闲多种竹_买春赏雨在茅屋_醉时卧倒杏花边_怕听莺儿惊梦熟"},
  {"number": 15, "name": "唐明皇游月宫", "verse": "仙槎一叶泛中流_月殿蟾宫任尔游_盈耳霓裳声暂歇_酒诗吟饮几时休"},
  {"number": 16, "name": "牧童跨犊归家", "verse": "天边鸦背夕阳回_陇外儿童跨犊来_姜笛频吹声切耳_短长腔调乐何哉"},
  {"number": 17, "name": "萧何月下追韩信", "verse": "秋水蒹葭白露盈_盈庭月色浸阶清_清风吹动马铃响_响接晨钟不断声"},
  {"number": 18, "name": "杜鹃泣血动客心", "verse": "杜鹃啼血泪悲声_声怨霜寒梦户惊_惊动异乡为异客_客心更触故园情"},
  {"number": 19, "name": "伏羲画八卦", "verse": "干卦三连号太阳_潜龙勿用第一章_其中爻象能参透_百福骈臻大吉昌"},
  {"number": 20, "name": "雪梅招亲", "verse": "天上仙花难问种_人间尘事几多更_前程已注公私簿_罚赏分明浊与清"}
]

# Example input and responding output:

## Example Input
{
    "出生日期": "2002-11-20",
    "出生时间": "20:30:00",
    "出生地": "广东省深圳市南山区"
}

## Example Output
{
    "career_personality": "去年流年事业运，概述用户的事业性格及其2024年事业大运（约120字，带有回顾的意味）",
    
    "annual_fortune": "用户2025年的事业大运，概述今年流年事业运的整体情况（约120字，包含机会点和风险）。",
    
    "monthly_fortune": {
        "January": {
            "score": 87,
            "fortune": "约80字描述月度事业运势及建议，语言基调为正能量。"
        },
        "February": {
            "score": 90,
            "fortune": "约80字描述月度事业运势及建议，语言基调为正能量。"
        },
        [... 其他月份省略 ...]
        "December": {
            "score": 70,
            "fortune": "约80字描述月度事业运势及建议，语言基调为正能量。"
        }
    },
    
    "career_signature": "根据当前时间，采用梅花易数从签文库中抽取的签文（verse）",
    
    "annual_summary": "结合灵签内容和事业性格与流年运势，从人际关系、团队合作、自我发展等维度提供约200字的正能量建议。"
}

# Remember:
- 输出必须严格遵循JSON格式
- 所有预测和建议都应保持积极正面的基调
- 月度运势必须包含具体分数和详细描述
- 确保抽取的灵签来自提供的签文库
""";
        
        String userPrompt;
        if (dto.getBirthTime() != null) {
            userPrompt = String.format("接下来请根据以下信息生成运势：出生日期：%s，出生时间：%s，出生地点：%s",
                dto.getBirthDate(), dto.getBirthTime(), dto.getBirthPlace());
        } else {
            userPrompt = String.format("接下来请根据以下信息生成运势：出生日期：%s，出生地点：%s",
                dto.getBirthDate(), dto.getBirthPlace());
        }
        
        String fortune = chatLanguageModel.generate(systemPrompt + "\n" + userPrompt);

        try {
            // 处理返回的文本，提取JSON内容
            String jsonContent = fortune;
            if (fortune.startsWith("```json")) {
                jsonContent = fortune.substring(fortune.indexOf('{'), fortune.lastIndexOf('}') + 1);
            }

            // 解析JSON响应
            JsonNode fortuneJson = objectMapper.readTree(jsonContent);

            // 填充运势信息到information对象
            information.setCareerPersonality(fortuneJson.get("career_personality").asText());
            information.setAnnualFortune(fortuneJson.get("annual_fortune").asText());
            information.setMonthlyFortune(fortuneJson.get("monthly_fortune").toString());
            information.setCareerSignature(fortuneJson.get("career_signature").asText());
            information.setAnnualSummary(fortuneJson.get("annual_summary").asText());

            // 只有当openid不是testopenid时才保存到数据库
            if (!"testopenid".equals(dto.getOpenid())) {
                save(information);
            }
            
        } catch (JsonProcessingException e) {
            log.error("解析运势JSON失败", e);
            throw new RuntimeException("解析运势失败", e);
        }

        return information;
    }

    @Override
    public Information getInformationByOpenid(InformationDTO dto) {
        log.info("Getting information for openid: {}", dto.getOpenid());
        
        // 使用 MybatisPlus 的 lambdaQuery 方法构建查询
        return this.lambdaQuery()
                .eq(Information::getOpenid, dto.getOpenid())
                .one();  // 获取单条记录，如果没有找到会返回 null
    }

}
