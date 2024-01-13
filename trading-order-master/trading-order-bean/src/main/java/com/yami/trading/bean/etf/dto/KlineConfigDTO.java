package com.yami.trading.bean.etf.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.RobotModel;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * etfK线图配置表DTO
 *
 * @author lucas
 * @version 2023-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KlineConfigDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /**
     * uuid
     */
    @ApiModelProperty("uuid")
    private String uuid;
    /**
     * 交易对
     */
    @NotBlank
    @ApiModelProperty("交易对")
    private String symbol;

    @NotBlank
    @ApiModelProperty("交易对名称")
    private String symbolName;
    /**
     * 开市时间
     */
    @NotNull
    @ApiModelProperty("开市时间")
    private Long openTimeTs;
    /**
     * 闭市时间
     */
    @ApiModelProperty("闭市时间")
    private Long closeTimeTs;
    /**
     * 开盘价
     */
    @NotNull
    @ApiModelProperty("开盘价")
    private BigDecimal openPrice;
    /**
     * 收盘价
     */
    @NotNull
    @ApiModelProperty("收盘价")
    private BigDecimal closePrice;
    /**
     * 今日最高价
     */
    @NotNull
    @ApiModelProperty("今日最高价")
    private BigDecimal high;
    /**
     * 今日最低价
     */
    @NotNull
    @ApiModelProperty("今日最低价")
    private BigDecimal low;
    /**
     * 今日最高成交量
     */
    @NotNull
    @ApiModelProperty("今日最高成交量")
    private BigDecimal turnoverHigh;
    /**
     * 今日最低成交量
     */
    @NotNull
    @ApiModelProperty("今日最低成交量")
    private BigDecimal turnoverLow;
    /**
     * 控盘策略
     */
    @NotNull
    @ApiModelProperty("控盘策略")
    private Integer strategy;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("随机模型的uuid")
    private String robot_model_uuid;

}
