package com.yami.trading.common.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yami.trading.common.util.DateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/*
 * 数据Entity类
 * @author lucas
 * @version 2023-03-08
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BaseEntity extends UUIDEntity {

    private static final long serialVersionUID = 1L;
    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTimeTs;
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
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTimeTs;

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
    /**
     * 默认构造函数
     */
    
    public BaseEntity () {

    }

}
