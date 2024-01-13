package com.yami.trading.bean.item.domain;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 用户自选Entity
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_item_user_optional")
public class ItemUserOptional extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * SYMBOL
     */
	private String symbol;
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

	private String pid;
}
