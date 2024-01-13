package com.yami.trading.bean.c2c;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@TableName("t_c2c_appeal")
@Data
public class C2cAppeal extends UUIDEntity {

    private String orderNo;

    /**
     * 申诉理由
     */
    private String reason;

    /**
     * 申诉描述
     */
    private String description;

    /**
     * 申诉凭证
     */
    private String img;

    /**
     * 联系人
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 申诉状态：0已提交/1已处理
     */
    private String state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
