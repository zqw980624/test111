package com.yami.trading.bean.purchasing;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_project_variety")
public class ProjectVariety extends UUIDEntity {

    /**
     * 股票代码
     */
    private String relatedStockSymbol;

    /**
     * 交易对
     */
    private String transactionPairsSymbol;

    /**
     * 股票代码名称
     */
    private String relatedStockSymbolName;
    /**
     * 初始化价格
     */
    private BigDecimal initPrice;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("逻辑删除标记")
    private Integer delFlag;

    @ApiModelProperty("状态 1上架 0下架")
    private int status;

    @ApiModelProperty("项目总类id")
    private  String projectBreedId;

    @ApiModelProperty("持仓占比")
    private  BigDecimal  positionProportion;

    @TableField(exist = false)
    private Realtime realtime;

    @ApiModelProperty("数据源类别  1 机器人 2  第三方数据采集")
    private int dataType;

    @TableField(exist = false)
    @ApiModelProperty("相关股票品种")
    private String relatedStockName;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建日期")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新日期")
    private Date updateTime;
}
