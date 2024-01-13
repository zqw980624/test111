package com.yami.trading.api.controller;

import com.yami.trading.bean.c2c.C2cAppeal;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.c2c.C2cAppealService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

/**
 * C2C申诉
 */
@RestController
@CrossOrigin
public class ApiC2cAppealController {
    @Autowired
    private C2cAppealService c2cAppealService;
    @Autowired
    private C2cOrderService c2cOrderService;
    @Autowired
    private UserService partyService;
    @Autowired
    private TipService tipService;
    private final String action = "/api/c2cAppeal!";
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    /**
     * 申诉申请
     */
    @RequestMapping(action + "apply.action")
    public Result apply(HttpServletRequest request) throws IOException {
        String order_no = request.getParameter("order_no");
        String reason = request.getParameter("reason");
        String description = request.getParameter("description");
        String img = request.getParameter("img");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        if (StringUtils.isEmptyString(order_no)) {
            throw new YamiShopBindException("申诉订单号不正确");
        }
        if (StringUtils.isEmptyString(reason)) {
            throw new YamiShopBindException("请输入申诉原因");
        }
        if (StringUtils.isEmptyString(img)) {
            throw new YamiShopBindException("请上传申诉凭证");
        }
        String partyId = SecurityUtils.getUser().getUserId();
        C2cOrder order = this.c2cOrderService.get(order_no);
        if (null == order || !partyId.equals(order.getPartyId())) {
            throw new YamiShopBindException("订单不存在");
        }
        C2cAppeal appeal = this.c2cAppealService.findByOrderNo(order_no);
        if (null != appeal) {
            throw new YamiShopBindException("该订单已提交申诉");
        }
        C2cAppeal entity = new C2cAppeal();
        entity.setOrderNo(order_no);
        entity.setReason(reason);
        entity.setDescription(description);
        entity.setImg(img);
        entity.setName(name);
        entity.setPhone(phone);
        entity.setState("0");
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        this.c2cAppealService.save(entity);
        order.setState("2");
        c2cOrderService.updateById(order);
        User party = this.partyService.getById(partyId);
        if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRoleName())) {
            this.tipService.saveTip(entity.getUuid().toString(), TipConstants.C2C_APPEAL);
        }
        return Result.succeed();
    }

    /**
     * 获取 申诉详情
     */
    @RequestMapping(action + "get.action")
    public Result get(HttpServletRequest request) throws IOException {
        String order_no = request.getParameter("order_no");
        if (StringUtils.isEmptyString(order_no)) {
            throw new YamiShopBindException("申诉订单号不正确");
        }
        C2cAppeal c2cAppeal = this.c2cAppealService.findByOrderNo(order_no);
        if (null == c2cAppeal) {
            throw new YamiShopBindException("申诉不存在");
        }
        if (StringUtils.isNotEmpty(c2cAppeal.getImg())) {
            String path = awsS3OSSFileService.getUrl(c2cAppeal.getImg());
            c2cAppeal.setImg(path);
        }
        return Result.succeed();
    }
}
