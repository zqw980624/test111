package com.yami.trading.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_tip")
public class Tip  extends UUIDEntity {


    /**
     *
     */
    private static final long serialVersionUID = 2944570190286781603L;
    /**
     * 模块
     */
    private String model;
    /**
     * 业务id
     */
    private String businessId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 时间戳
     */
    private Long timeStamp;
    /**
     * 指定用户
     */
    private String targetUsername;

}
