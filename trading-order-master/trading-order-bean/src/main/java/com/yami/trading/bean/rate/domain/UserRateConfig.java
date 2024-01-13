package com.yami.trading.bean.rate.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 用户汇率管理Entity
 * @author lucas
 * @version 2023-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_rate_config")
public class UserRateConfig extends UUIDEntity {

	private static final long serialVersionUID = 1L;
	/**
     * OUT_OR_IN
     */
	private String outOrIn;
	/**
     * CURRENCY
     */
	private String currency;
	/**
     * PARTY_ID
     */
	private String partyId;

}
