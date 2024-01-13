package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tz_sms_log")
public class SmsLog implements Serializable {
    private static final long serialVersionUID = 2090714647038636896L;

    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;

    private String userId;

    /**
     * 手机
     */
    private String userPhone;

    /**
     * 内容
     */

    private String content;

    /**
     * CODE
     */

    private String mobileCode;

    /**
     *  时间
     */

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date recDate;

    /**
     *
     */

    private String type;

    /**
     * status
     */

    private String status;

    /**
     * response_code
     */

    private String responseCode;


}
