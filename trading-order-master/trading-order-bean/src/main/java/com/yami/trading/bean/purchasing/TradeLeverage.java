package com.yami.trading.bean.purchasing;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易杠杆
 */

@Data
@TableName("t_trade_leverage")
public class TradeLeverage {

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
    private int delFlag;


    /**
     * 交易倍数
     */
    @ApiModelProperty("交易倍数")
        private BigDecimal multiple;
    /**
     * 申购id
     */
    @ApiModelProperty("申购id")
    private  String purchasingId;
}
