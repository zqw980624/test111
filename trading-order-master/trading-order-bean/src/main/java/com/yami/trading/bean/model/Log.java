package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_log")
public class Log  extends BaseEntity {

    /**
     * Member Description
     */

    private static final long serialVersionUID = 1837652077217320806L;
    /**
     * 日志归属
     */
    private String userId;
    /**
     * 日志归属
     */
    private String username;

    // 日志类型，见Constants
    private String category;
    /**
     * 日志
     */
    private String log;

    /**
     * 扩展信息，统计分类时使用
     */
    private String extra;

    private String operator;
}
