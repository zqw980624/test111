package com.yami.trading.bean.purchasing;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 *
 */

@Data
@TableName("t_project_breed")
public class ProjectBreed  extends BaseEntity {



    private  String projectTypeSymbol;

    private  String projectTypeName;

    /**
     * 代码
     */
    private  String code;

    /**
     * 数据源类别 1 机器人 2  第三方数据采集
     */
    private  int dataType;

    /**
     * 交易对
     */
    private  String  transactionPairsSymbol;
    /**
     * 每张金额
     */

    private  BigDecimal amount;
    /**
     * 每张手续费
     */
    private  BigDecimal fee;
    /**
     * 最小变动单位
     */
    private  String minUnit;
    /**
     * 最小变动单位的盈亏金额
     */
    private BigDecimal minProfitLoss;
    /**
     * 项目名称
     */
    private  String projectName;

    /**
     * 成交量
     */
    private  BigDecimal turnover;
    /**
     * 初始化价格
     */
    private  BigDecimal initPrice;
    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;


    private String relatedStockVarieties;


    private  int status;
}

