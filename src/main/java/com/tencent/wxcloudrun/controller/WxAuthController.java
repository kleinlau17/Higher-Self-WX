package com.tencent.wxcloudrun.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import com.tencent.wxcloudrun.mapper.InformationMapper;
import com.tencent.wxcloudrun.dto.WxAuthDTO;

@RestController
@RequestMapping("/wx")
public class WxAuthController {
    
    private static final String APP_ID = "wx2584d463c0bb7200";
    private static final String APP_SECRET = "4f07200792becd8348ed877f59b9c2b2";
    private static final String WX_AUTH_URL = "https://api.weixin.qq.com/sns/jscode2session";
    
    @Resource
    private RestTemplate restTemplate;
    
    @Resource
    private InformationMapper informationMapper;
    
    @PostMapping("/openId")
    public ResponseEntity<?> getOpenId(@RequestBody WxAuthDTO request) {
        String code = request.getCode();
        
        if (StringUtils.isEmpty(code)) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", "Missing code in request body"));
        }
        
        // 测试用code逻辑
        if ("testcode".equals(code)) {
            return ResponseEntity.ok()
                .body(Collections.singletonMap("openid", "testopenid"));
        }
        
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            WX_AUTH_URL, APP_ID, APP_SECRET, code);
            
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body != null && body.containsKey("openid")) {
                String openid = (String) body.get("openid");
                System.out.println("openid: " + openid);
                
                // 查询数据库中是否存在该openid
                QueryWrapper<Information> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("openid", openid);
                Information info = informationMapper.selectOne(queryWrapper);
                System.out.println("info: " + info);
                
                // 如果存在则返回-1，否则返回原始openid
                String returnOpenid = (info != null) ? "-1" : openid;
                
                return ResponseEntity.ok()
                    .body(Collections.singletonMap("openid", returnOpenid));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to get openid"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Internal Server Error")); 
        }
    }

    @PostMapping("/getOpenId")
    public ResponseEntity<Map<String, Object>> getOpenId2(@RequestBody WxAuthDTO request) {
        if (request == null || StringUtils.isEmpty(request.getCode())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing code in request body");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                WX_AUTH_URL, APP_ID, APP_SECRET, request.getCode());

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient httpClient = HttpClients.custom()
                .setSslContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

            HttpComponentsClientHttpRequestFactory factory = 
                new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(5000);

            RestTemplate customRestTemplate = new RestTemplate(factory);

            ResponseEntity<Map> response = customRestTemplate.getForEntity(url, Map.class);
            Map<String, Object> weChatResponse = response.getBody();

            if (weChatResponse != null && weChatResponse.containsKey("openid")) {
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("openid", weChatResponse.get("openid"));
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to get openid");
                errorResponse.put("details", weChatResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
