package com.yami.trading.api.controller.exchange;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yami.trading.bean.exchange.PartyBlockchain;
import com.yami.trading.bean.model.ChannelBlockchain;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.MD5;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.ChannelBlockchainService;
import com.yami.trading.service.exchange.PartyBlockchainService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Api(tags = "区块链 -api")
public class ApiChannelBlockchainController {
    @Autowired
    private SysparaService sysparaService;
    private final String action = "/api/channelBlockchain!";
    @Autowired
    private ChannelBlockchainService channelBlockchainService;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private final String threedUrl = "https://api.star-pay.vip/api/gateway/pay";
    @Autowired
    UserService userService;
    @Autowired
    PartyBlockchainService partyBlockchainService;

    /**
     * 获取所有链地址
     */
    @RequestMapping(action + "list.action")
    public Object list() throws IOException {
        List<ChannelBlockchain> data = new ArrayList<ChannelBlockchain>();
        String partyId = SecurityUtils.getUser().getUserId();
        User party = userService.getById(partyId);
        List<PartyBlockchain> list = partyBlockchainService.findByUserName(party.getUserName());
        if (null != list && !list.isEmpty()) {
            data = list.stream().map(dict -> {
                String qrImage = dict.getQrImage();
                String chainAddress = dict.getAddress();
                String chainName = dict.getChainName();
                String coinSymbol = dict.getCoinSymbol();
                String autoStr = dict.getAuto();
                boolean auto = autoStr.equals("Y") ? true : false;
                ChannelBlockchain cbc = new ChannelBlockchain();
                cbc.setBlockchainName(chainName);
                cbc.setBlockchain_name(chainName);
                cbc.setAddress(chainAddress);
                cbc.setCoin(coinSymbol);
                cbc.setAuto(auto);
                cbc.setImg(qrImage);
                return cbc;
            }).collect(Collectors.toList());
        }
        if (data.isEmpty()) data = channelBlockchainService.list();
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setBlockchain_name(data.get(i).getBlockchainName());
            if (1 == this.sysparaService.find("can_recharge").getInteger()) {
                // 允许在线充值，展示二维码
                if (!StringUtils.isNullOrEmpty(data.get(i).getImg())) {
                    String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + data.get(i).getImg();
                    data.get(i).setImg(path);
                }
            } else {
                data.get(i).setImg(null);
                data.get(i).setAddress(null);
            }
        }
        return Result.succeed(data);
    }

    /**
     * 根据币种获取链地址
     */
    @GetMapping(action + "getBlockchainName.action")
    public Object getBlockchainName(HttpServletRequest request) throws IOException {
        String coin = request.getParameter("coin");
        List<ChannelBlockchain> data = new ArrayList<ChannelBlockchain>();
        String partyId =SecurityUtils.getUser().getUserId();
        User party = userService.getById(partyId);
        if (0 == this.sysparaService.find("can_recharge").getInteger()) {
            return Result.failed("请联系客服充值");
        }

        List<PartyBlockchain> list = partyBlockchainService.findByUserNameAndCoinSymbol(party.getUserName(), coin);
        if (null != list && !list.isEmpty()) {
            data = list.stream().map(dict -> {
                String qrImage = dict.getQrImage();
                String chainAddress = dict.getAddress();
                String chainName = dict.getChainName();
                String coinSymbol = dict.getCoinSymbol();
                String autoStr = dict.getAuto();
                boolean auto = autoStr.equals("Y") ? true : false;
                ChannelBlockchain cbc = new ChannelBlockchain();
                cbc.setBlockchain_name(chainName);
                cbc.setAddress(chainAddress);
                cbc.setCoin(coinSymbol);
                cbc.setAuto(auto);
                cbc.setImg(qrImage);
                return cbc;
            }).collect(Collectors.toList());
        }
        if (data.isEmpty()) data = this.channelBlockchainService.findByCoin(coin.toLowerCase());
        for (int i = 0; i < data.size(); i++) {
            data.get(i).setBlockchain_name(data.get(i).getBlockchainName());
            if (1 == this.sysparaService.find("can_recharge").getInteger()) {
                if (!StringUtils.isNullOrEmpty(data.get(i).getImg())) {
                    String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + data.get(i).getImg();
                    data.get(i).setImgStr("/public/showimg!showImg.action?imagePath=" + data.get(i).getImg());
                    data.get(i).setImg(path);
                }
            } else {
                data.get(i).setImg(null);
                data.get(i).setImgStr(null);
                data.get(i).setAddress(null);
            }
        }
        return Result.succeed(data);
    }

    /**
     * 根据第三方充值链接
     */
    @RequestMapping(action + "getThirdUrl.action")
    public Result getThirdUrl(HttpServletRequest request) throws IOException {

        String key = this.sysparaService.find("third_key").getSvalue();
        String merchant_no = this.sysparaService.find("third_id").getSvalue();
        int i = (int) (Math.random() * 900) + 100;
        //商户号
        //10位时间戳
        String timestamp = "" + System.currentTimeMillis() / 1000;
        String amount = request.getParameter("amount");
        //法币名称
        String fiat_currency = request.getParameter("fiat_currency");
        //订单号
        String merchant_ref = System.currentTimeMillis() + "" + i;
        //产品名称
        String product = "ERC20Buy";
        String sign_type = "MD5";
        Map<String, String> requestparam = new HashMap<>();
        JSONObject params = new JSONObject(new LinkedHashMap<>());
        JSONObject extra = new JSONObject(new LinkedHashMap<>());
        extra.put("fiat_currency", fiat_currency);
        params.put("merchant_ref", merchant_ref);
        params.put("product", product);
        params.put("amount", amount);
        params.put("extra", extra);
        String waitsign = merchant_no + params + sign_type + timestamp + key;
        String sign = MD5.sign(waitsign);
        requestparam.put("merchant_no", merchant_no);
        requestparam.put("params", params.toJSONString());
        requestparam.put("sign", sign);
        requestparam.put("sign_type", sign_type);
        requestparam.put("timestamp", params.toJSONString());
        String result = "";
        RequestBody body = new FormBody.Builder()
                .add("merchant_no", merchant_no)
                .add("timestamp", timestamp)
                .add("sign_type", sign_type)
                .add("params", params.toString())
                .add("sign", sign)
                .build();
        Request requestParm = new Request.Builder()
                .url(threedUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = okHttpClient.newCall(requestParm).execute();
            result = response.body().string();
        } catch (Exception e) {
           throw  new YamiShopBindException("请求第三方失败");
        }
        JSONObject resultJson = JSON.parseObject(result);
        Integer code = resultJson.getInteger("code");
        if (code == 200) {
            JSONObject resultParams = resultJson.getJSONObject("params");
            return Result.succeed(resultParams.getString("payurl"));
        } else {
            return Result.failed(resultJson.getString("message"));

        }
    }
}
