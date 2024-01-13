package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 用户实名认证
 */

@Data
@TableName("tz_real_name_auth_record")
public class RealNameAuthRecord extends BaseEntity {

    private  String userId;


    /**
     * 国籍
     */
    private String nationality;

    /**
     * ID名称，如身份证等
     */
    private String idName;
    /**
     * 证件号码
     */
    private String idNumber;
    /**
     * 实名姓名
     */
    private String name;
    /**
     * 证件正面照
     */
    private String idFrontImg;
    /**
     * 证件背面照
     */
    private String idBackImg;

    /**
     * 手持证件正面照
     */
    private String handheldPhoto;

    /**
     * 0已申请未审核 ，1.审核中 2 审核通过,3审核未通过
     */
    private int status;
    /**
     * 审核消息，未通过原因
     *
     */
    private String msg;
    /**
     * 审核时间
     */
    private Date operationTime;
}


