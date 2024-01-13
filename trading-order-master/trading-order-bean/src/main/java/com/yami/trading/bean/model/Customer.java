package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_customer")
public class Customer  extends UUIDEntity {

    /**
     * 客服名称
     */
    private String userName;
    /**
     * 在线状态,0:下线，1：在线
     */
    private int onlineState;
    /**
     * 最后一次分配的时间
     */
    private Date lastCustomerTime;
    /**
     * 最后一次分配的用户
     */
    private String lastMessageUser;
    /**
     * 最后一次上线的时间
     */
    private Date lastOnlineTime;
    /**
     * 最后一次下线的时间
     */
    private Date lastOfflineTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 自动回复语句
     */
    private String autoAnswer;
}
