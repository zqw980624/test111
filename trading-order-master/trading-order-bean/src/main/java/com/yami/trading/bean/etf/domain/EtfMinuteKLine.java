package com.yami.trading.bean.etf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * Etf分钟级k线图
 * </p>
 *
 * @author lucas
 * @since 2023-06-17 20:18:56
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_etf_minute_k_line")
public class EtfMinuteKLine {

    @TableId(type = IdType.ASSIGN_ID)
    private String uuid;

    /**
     * 配置的configId
     */
    private String symbol;

    /**
     * 这一分钟的时间戳
     */
    private Long ts;

    private BigDecimal high;

    private BigDecimal low;

    @TableField("`open`")
    private BigDecimal open;

    @TableField("`close`")
    private BigDecimal close;

    private BigDecimal amount;

    private BigDecimal volume;


}
