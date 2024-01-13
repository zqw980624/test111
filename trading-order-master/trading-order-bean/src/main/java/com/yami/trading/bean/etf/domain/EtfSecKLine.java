package com.yami.trading.bean.etf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * Etf秒级k线图
 * </p>
 *
 * @author HT
 * @since 2023-05-18 17:27:13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_etf_sec_k_line")
public class EtfSecKLine {

    /**
     * 实体主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String uuid;


    private String symbol;

    /**
     * 这一秒的时间戳
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
