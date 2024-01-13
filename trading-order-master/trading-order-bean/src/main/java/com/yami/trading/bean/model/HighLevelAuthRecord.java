package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.*;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 高级认证
 */

@TableName("tz_high_level_auth_record")
@Data
public class HighLevelAuthRecord  {


    /**
     * 实体主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String uuid;


    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;



    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    private String userId;
    /**
     * 工作地址
     */
    private String workPlace;
    /**
     * 家庭地址
     */
    private String homePlace;
    /**
     * 亲属关系
     */
    private String relativesRelation;
    /**
     * 亲属名称
     */
    private String relativesName;
    /**
     * 亲属地址
     */
    private String relativesPlace;
    /**
     * 亲属电话
     */
    private String relativesPhone;

    /**
     * 0已申请未审核 ，1审核中 ，2 审核通过,3审核未通过
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
