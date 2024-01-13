package com.yami.trading.bean.robot.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 下单机器人DTO
 *
 * @author lucas
 * @version 2023-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RobotVO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /**
     * uuid
     */
    @NotNull(message = "uuid不能为空")
    @ApiModelProperty("uuid")
    private String uuid;
    /**
     * 交易对
     */
    @ApiModelProperty("交易对")
    private String symbol;
    /**
     * user_id
     */
    @ApiModelProperty("user_id")
    private String user;

    @ApiModelProperty("username")
    private String username;

    @ApiModelProperty("password")
    private String password;
    /**
     * 状态 1 正常 0 无效
     */
    @NotNull(message = "状态 1 正常 0 无效不能为空")
    @ApiModelProperty("状态 1 正常 0 无效")
    private Integer status;
    /**
     * 价格变化步长
     */
    @ApiModelProperty("价格变化步长")
    private String step;
    /**
     * 最大下单间隔,单位秒
     */
    @ApiModelProperty("最大下单间隔,单位秒")
    private Long maxmunInterval;
    /**
     * 最小下单间隔,单位秒
     */
    @ApiModelProperty("最小下单间隔,单位秒")
    private Long minmunInterval;
    /**
     * 最大下单数量
     */
    @ApiModelProperty("最大下单数量")
    private Long maxmunNum;
    /**
     * 最小下单数量
     */
    @ApiModelProperty("最小下单数量")
    private Long minmunNum;
    /**
     * 运行状态
     */
    @ApiModelProperty("运行状态")
    private Long runningStatus;
    /**
     * 跟随大盘
     */
    @ApiModelProperty("跟随大盘")
    private String followMarket;
    /**
     * 大盘涨幅
     */
    @ApiModelProperty("大盘涨幅")
    private String marketIncrease;
    /**
     * 扩大倍数
     */
    @ApiModelProperty("扩大倍数")
    private String multiple;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("修改时间")
    private Date updateTime;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("交易对详细信息")
    private Item item;

}
