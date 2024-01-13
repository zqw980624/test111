package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_ip_menu")
public class IpMenu  extends UUIDEntity {

    /**
     * UID
     */
    private static final long serialVersionUID = -4196439149608747292L;

    /**
     * 白名单
     */
    public static final String IP_WHITE = "white";

    /**
     * 黑名单
     */
    public static final String IP_BLACK = "black";

    /**
     * 锁定名单
     */
    public static final String IP_LOCK = "lock";

    /**
     * ip
     */
    private String ip;

    /**
     * 类型 ：black:黑名单，white：白名单
     */
    private String type;

    /**
     * -1:标记删除，0:正常
     */
    private int deleteStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后处理时间
     */
    private Date lastOperaTime;
}
