package com.yami.trading.bean.item.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 产品杠杠倍数Entity
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_item_leverage")
public class ItemLeverage extends BaseEntity {

	private static final long serialVersionUID = 1L;
	/**
     * ITEM_ID
     */
	private String itemId;
	/**
     * LEVER_RATE
     */
	private String leverRate;
	/**
     * 创建时间
     */
	private Date createTime;
	/**
     * 更新时间
     */
	private Date updateTime;
	/**
     * 备注信息
     */
	private String remarks;

}
