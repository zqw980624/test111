package com.yami.trading.bean.etf.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author lucas
 * @since 2023-06-09 20:22:26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_etf_kline_stage_config")
public class EtfKlineStageConfig extends UUIDEntity {

    /**
     * 项目名称
     */
    @TableField("`name`")
    private String name;

    /**
     * 交易对
     */
    @NotBlank
    @ApiModelProperty(value = "交易对")
    private String symbol;

    /**
     * 开市时间
     */
    @ApiModelProperty(value = "开市时间")
    private Long openTimeTs;

    /**
     * 闭市时间
     */
    @ApiModelProperty(value = "闭市时间")
    private Long closeTimeTs;

    /**
     * 成交量
     */
    @ApiModelProperty(value = "成交量")
    private String turnover;

    /**
     * 最高价
     */
    @ApiModelProperty(value = "最高价")
    private String high;

    /**
     * 今日最低价
     */
    @ApiModelProperty(value = "今日最低价")
    private String low;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
