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

import java.sql.Time;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.hutool.http.HttpUtil;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import cn.hutool.http.HttpRequest;

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

    private final ChatLanguageModel chatLanguageModel;

    @Resource
    private ObjectMapper objectMapper;

    public InformationServiceImpl(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @Override
    public Information createInformation(InformationDTO dto) {
        Information information = new Information();
        BeanUtils.copyProperties(dto, information);

        // 设置当前年份为运势年份
        information.setFortuneYear(Calendar.getInstance().get(Calendar.YEAR));

        // 获取紫微斗数数据
        try {
            String izTroData = getIzTroData(dto);
            information.setZiwei(izTroData);
            log.info("成功获取紫微斗数数据: {}", izTroData);
        } catch (Exception e) {
            log.error("获取紫微斗数数据失败", e);
            throw new RuntimeException("获取紫微斗数数据失败", e);
        }

        // 获取八字数据
        try {
            String baziData = getBaziData(dto);
            information.setBazi(baziData);
            log.info("成功获取八字数据: {}", baziData);
        } catch (Exception e) {
            log.error("获取八字数据失败", e);
            throw new RuntimeException("获取八字数据失败", e);
        }

        // 使用 ChatLanguageModel 生成运势内容
        String systemPrompt = """
    
# Role:
你是一位专业的事业运势指导师，能够根据用户的生辰八字、紫微斗数等信息进行命理学分析，进而为用户提供个性化的事业运势分析及发展建议。

# Output:
输出必须为JSON格式，包含以下字段：
- `career_personality`: 事业性格及2024年运势回顾（约120字）。
- `annual_fortune`: 2025年整体事业运势预测（约120字）。
- `monthly_fortune`: 每月运势详情，包含分数、事业运势描述和风险提示（约100字）。
- `career_signature`: 从好运信号库中抽取的好运信号（verse）。
- `annual_summary`: 根据好运信号、事业性格及流年运势，提供正能量建议（约200字）。

# Workflow:
1. 接收用户输入（生辰八字、紫微斗数、当前时间）。
2. 事业性格分析：结合生辰八字和紫微斗数，分析用户的事业性格及适合的发展方向。
3. 流年事业运预测：概述2024年与2025年的事业运势，并推算整体流年事业运。
4. 每月事业运势分析：按月给出事业运势分数和正能量建议。
5. 抽取好运信号：基于梅花易数从好运信号库中抽取一条适配的好运信号。
6. 事业建议总结：结合好运信号、事业性格和流年运势，为用户提供关于人际关系、团队合作和自我发展的建议。
7. 输出JSON格式的结果。

# 提示：
- 所有运势分析及建议均保持积极正向的基调，并确保内容不受限于特定行业。
- 请使用通俗易懂的语言，避免使用晦涩的命理术语，同时保持内容的专业性和深度。
- 月度运势必须包含具体分数和详细描述。
- 输出结果将以JSON格式返回，方便用户进一步使用。
- 确保抽取的好运信号来自提供的好运信号库。

# 好运信号库:
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

# Input Format:
- 生辰八字
- 紫微斗数
- 当前时间

## Input Example
{
    "生辰八字": "（具体数据）",
    "紫微斗数": "（具体数据）",
    "当前时间": "19:30:00"
}

## Output Format
{
    "career_personality": "去年流年事业运，概述用户的事业性格及其2024年事业大运（约120字，带有回顾的意味）",
    
    "annual_fortune": "用户2025年的事业大运，概述今年流年事业运的整体情况（约120字，包含机会点和风险）。",
    
    "monthly_fortune": {
        "January": {
            "score": 87,
            "fortune": "约100字描述月度事业运势及建议，语言基调为正能量。"
        },
        "February": {
            "score": 90,
            "fortune": "约100字描述月度事业运势及建议，语言基调为正能量。"
        },
        [... 其他月份省略 ...]
        "December": {
            "score": 70,
            "fortune": "约100字描述月度事业运势及建议，语言基调为正能量。"
        }
    },
    
    "career_signature": "根据当前时间，采用梅花易数从好运信号库中抽取的好运信号（verse）",
    
    "annual_summary": "结合好运信号内容和事业性格与流年运势，从人际关系、团队合作、自我发展等维度提供正能量建议。并以钩子文案收尾，引导用户主动加强自我探索，深入了解自己的事业性格。整段输出约200字。"
}

## Output Example
{
    "career_personality": "过去的一年你始终面对着外部环境波橘云诡的变化，犹如滔天风浪里的夜航船。在这个充满变化的时代里，我们试图去寻找确定性与安全感。但如果你不松开紧握的拳头，又怎么能接纳命运新的馈赠呢？",
    
    "annual_fortune": "请以全新的你迎接属于你的精彩吧！拥抱变化将会是这一年的主旋律。你可以去到新的环境、尝试不同的方向、开拓新的业务。不要害怕蹒跚的起步，开端总是不完美的，能通向目的地就足够了。",
    
    "monthly_fortune": {
        "January": {
            "score": 77,
            "fortune": "你会收到一些新的机会，但对你来说并没有足够的吸引力。不要就此灰心，继续保持足够的外部连接与信息搜集，合适的机会离你并不远。"
        },
        "February": {
            "score": 90,
            "fortune": "你会真切地感到一种推背感，仿佛有无形的力量将你牵引至高处。继续保持谦卑和低调，空的容器才能容纳更多。有时，幸福者避让原则是对你的保护。"
        },
        [... 其他月份省略 ...]
        "December": {
            "score": 65,
            "fortune": "也许你会面临外部的质疑和挑战，身边不怀好意的试探和流言蜚语。不要害怕，有时闭上眼睛和耳朵是对自己的保护。保持足够的定力，当旋风停止，你才能看清真相。"
        }
    },
    
    "career_signature": "根据当前时间，采用梅花易数从好运信号库中抽取的好运信号（verse）",
    
    "annual_summary": "2025年上半年需要注意身体健康与人际关系，学会在复杂的处境中养护自己的身心，切莫陷入无所谓的消耗当中。挖掘自己做某些事毫不费力的天赋才华，了解自己的事业性格，为下半年即将到来的新机会做好准备。许多时候，事情远没有你想象中复杂。"
}

        """;

        // 获取当前时间
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeFormat.format(new Date());

        String userPrompt = String.format("接下来请根据以下信息生成内容：\n 生辰八字：%s\n 紫微斗数：%s\n 当前时间：%s",
                information.getBazi(), information.getZiwei(), currentTime);

        String response = chatLanguageModel.generate(systemPrompt + "\n" + userPrompt);

        log.info("API原始响应：{}", response); // 调试用，正式环境可改为trace级别

        try {
            // 处理返回的文本，提取JSON内容
            String jsonContent = response;
            if (response.startsWith("```json")) {
                jsonContent = response.substring(response.indexOf('{'), response.lastIndexOf('}') + 1);
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
            log.error("JSON解析失败，原始响应：{}", response, e);
            throw new RuntimeException("运势内容生成异常", e);
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

    public String getIzTroData(InformationDTO dto) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String solarDateStr = dateFormat.format(dto.getBirthDate());

            // 计算时辰索引（1-12）
            int hour = dto.getBirthTime().toLocalTime().getHour();
            int timeIndex = hour / 2 + 1;

            String genderParam = "male".equalsIgnoreCase(dto.getGender()) ? "male" : "female";

            String url = String.format(
                "https://express-nsp5-135695-9-1336124445.sh.run.tcloudbase.com/api/iztro?solarDateStr=%s&timeIndex=%d&gender=%s",
                solarDateStr, timeIndex, genderParam
            );

            // 发送请求并解析响应
            String response = HttpUtil.get(url);
            JsonNode jsonNode = objectMapper.readTree(response);

            // 只返回content字段的内容
            return jsonNode.get("content").asText();

        } catch (Exception e) {
            log.error("获取紫微斗数数据异常 参数：{}", dto, e);
            throw new RuntimeException("紫微斗数排盘服务调用失败", e);
        }
    }

    public String getBaziData(InformationDTO dto) {
        try {
            String appcode = "9378c5c5e01f479e8046a564f0c16acf";
            String url = "https://jisubazi.market.alicloudapi.com/bazi/paipan";
            
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("city", dto.getBirthPlace());
            paramMap.put("year", getYearFromDate(dto.getBirthDate()));
            paramMap.put("month", getMonthFromDate(dto.getBirthDate()));
            paramMap.put("day", getDayFromDate(dto.getBirthDate()));
            paramMap.put("hour", getHourFromTime(dto.getBirthTime()));
            paramMap.put("minute", getMinuteFromTime(dto.getBirthTime()));
            paramMap.put("sex", "male".equalsIgnoreCase(dto.getGender()) ? "1" : "0");
            paramMap.put("name", "用户");
            paramMap.put("islunar", "0");
            paramMap.put("istaiyang", "0");

            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + appcode);
            headers.put("Content-Type", "application/json; charset=UTF-8");

            String response = HttpRequest.get(url)
                .form(paramMap)
                .timeout(5000)
                .addHeaders(headers)
                .execute()
                .body();
                
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("result").toString();
            
        } catch (Exception e) {
            log.error("获取八字数据异常 参数：{}", dto, e);
            throw new RuntimeException("八字排盘服务调用失败", e);
        }
    }

    // 辅助方法
    private int getYearFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    private int getMonthFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    private int getDayFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    private int getHourFromTime(Time time) {
        return time.toLocalTime().getHour();
    }

    private int getMinuteFromTime(Time time) {
        return time.toLocalTime().getMinute();
    }

}
