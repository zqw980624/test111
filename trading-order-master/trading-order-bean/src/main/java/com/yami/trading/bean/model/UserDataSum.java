package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
@TableName("t_userdatasum")
public class UserDataSum  extends UUIDEntity {

    private static final long serialVersionUID = 1256269500666828481L;

    private String userId;
    /**
     * 推荐人推荐总数。 用户是4级，代理则是伞下所有推荐用户
     */
    private int recoNum;
    /**
     * 伞下用户充值总额（目前是4级）
     */
    private double rechargeSum;
}
