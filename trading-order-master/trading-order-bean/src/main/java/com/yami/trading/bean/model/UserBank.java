package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_user_bank")
public class UserBank extends UUIDEntity {
    private static final long serialVersionUID = -1883480204147696409L;

    private String userName;
    private String userId;
    private String bankName;
    private String bankNo;
    private String bankAddress;
    private String bankImg;
    private String bankPhone;
    private Date createTime;
    private String methodName;
}
