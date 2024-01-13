package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;


import java.util.Date;

/**
 *人工充值记录
 */


@TableName("t_user_safeword_apply")
@Data
public class UserSafewordApply extends BaseEntity {
    private String userId;
    /**
     * 证件正面照
     */
    private String idcardPathFront;
    /**
     * 证件背面照
     */
    private String idcardPathBack;
    /**
     * 正面手持证件照
     */
    private String idcardPathHold;
    /**
     * 资金密码
     */
    private String safeword;
    /**
     * 1审核中 ，2 审核通过,3审核未通过
     */
    private int status;
    /**
     * 审核消息，未通过原因
     */
    private String msg;
    /**
     * 审核时间
     */
    private Date applyTime;
    /**
     * 操作类型 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
     */
    private int operate;
    /**
     * 备注
     */
    private String remark;
}
