package com.yami.trading.admin.facade;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 1.机器翻译2.0,请填写在讯飞开放平台-控制台-对应能力页面获取的APPID、APISecret、APIKey。
 * 2.目前仅支持中文与其他语种的互译，不包含中文的两个语种之间不能直接翻译。
 * 3.翻译文本不能超过5000个字符，即汉语不超过15000个字节，英文不超过5000个字节。
 * 4.此接口调用返回时长上有优化、通过个性化术语资源使用可以做到词语个性化翻译、后面会支持更多的翻译语种。
 */
@Slf4j
@Component
public class MachineTranslationService {
    private static String requestUrl = "https://itrans.xf-yun.com/v1/its";
    // 控制台获取以下信息
    private static String APPID = "a2a7b5f5";
    private static String APISecret = "ZjMzODBjOWUwN2ZiMTcxM2FiNDA5MjNk";
    private static String APIKey = "bc00d9f28c7e9a3ad074f3b98aa56c73";
    // 术语资源唯一标识，请根据控制台定义的RES_ID替换具体值，如不需术语可以不用传递此参数
    private static final String RES_ID = "its_en_cn_word";
    // 翻译信息
    private static String FROM = "cn";// 源语种
    private static String TO = "en";// 目标语种
    // JSON解析
    private static final Gson gson = new Gson();

    // 主函数
    public String translate(String content) {
        long startTime = System.currentTimeMillis();
        MachineTranslationService demo = new MachineTranslationService();
        try {
            String resp = demo.doRequest(content);
            JsonParse myJsonParse = gson.fromJson(resp, JsonParse.class);
            String textBase64Decode = new String(Base64.getDecoder().decode(myJsonParse.payload.result.text), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(textBase64Decode);
            long endTime = System.currentTimeMillis();
            log.info("本次调用耗时: " + (endTime - startTime) + " ms");
            return jsonObject.getJSONObject("trans_result").getString("dst");
        } catch (Exception e) {
            log.error("翻译失败:" + e);
        }
        return null;

    }

    // 请求方法
    public String doRequest(String content) throws Exception {
        URL realUrl = new URL(buildRequetUrl());
        URLConnection connection = realUrl.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-type", "application/json");
        OutputStream out = httpURLConnection.getOutputStream();
        String params = buildParam(content);
        System.out.println("params=>" + params.replaceAll(" ", ""));
        out.write(params.getBytes());
        out.flush();
        InputStream is = null;
        try {
            is = httpURLConnection.getInputStream();
        } catch (Exception e) {
            is = httpURLConnection.getErrorStream();
            throw new Exception("make request error:" + "code is " + httpURLConnection.getResponseMessage() + readAllBytes(is));
        }
        return readAllBytes(is);
    }

    // 处理请求URL，包含鉴权
    public String buildRequetUrl() {
        URL url = null;
        // 替换调schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
        String httpRequestUrl = requestUrl.replace("ws://", "http://").replace("wss://", "https://");
        try {
            url = new URL(httpRequestUrl);
            //获取当前日期并格式化
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            //String date="Thu, 18 Nov 2021 03:05:18 GMT";
            String host = url.getHost();
           /* if (url.getPort()!=80 && url.getPort() !=443){
                host = host +":"+String.valueOf(url.getPort());
            }*/
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").//
                    append("date: ").append(date).append("\n").//
                    append("POST ").append(url.getPath()).append(" HTTP/1.1");
            Charset charset = Charset.forName("UTF-8");
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(APISecret.getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            //System.out.println(sha);
            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", APIKey, "hmac-sha256", "host date request-line", sha);
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            return String.format("%s?authorization=%s&host=%s&date=%s", requestUrl, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:" + e.getMessage());
        }
    }

    // 组装请求参数
    private String buildParam(String content) {
        String param = "{" + "    \"header\": {" + "        \"app_id\": \"" + APPID + "\"," + "        \"status\": 3," + "        \"res_id\": \"" + RES_ID + "\"" + "    }," + "    \"parameter\": {" + "        \"its\": {" + "            \"from\": \"" + FROM + "\"," + "            \"to\": \"" + TO + "\"," + "            \"result\": {}" + "        }" + "    }," + "    \"payload\": {" + "        \"input_data\": {" + "            \"encoding\": \"utf8\"," + "            \"status\": 3," + "            \"text\": \"" + Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)) + "\"" + "        }" + "    }" + "}";
        return param;
    }

    // 读取流数据
    private String readAllBytes(InputStream is) throws IOException {
        byte[] b = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int len = 0;
        while ((len = is.read(b)) != -1) {
            sb.append(new String(b, 0, len, "utf-8"));
        }
        return sb.toString();
    }

    // JSON解析
    class JsonParse {
        Payload payload;
    }

    class Payload {
        Result result;
    }

    class Result {
        String text;
    }
}