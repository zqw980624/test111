package com.yami.trading.common.dto;



import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class BaseDTO <T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实体主键
     */
    protected String uuid;

    /**
     * 创建日期
     */
    @ApiModelProperty("创建日期")
    protected Date createDate;

    /**
     * 创建人
     */
    protected String createBy;

    /**
     * 更新日期
     */
    protected Date updateDate;

    /**
     * 更新人
     */
    protected String updateBy;

    /**
     * 逻辑删除标记
     */
    @JsonIgnore
    protected Integer delFlag;

    /**
     * 构造函数
     */
    public BaseDTO () {

    }

    /**
     * 构造函数
     * @param id
     */
    public BaseDTO (String id) {
        this.uuid = id;
    }



}

