package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

/**
 * 区块链充值地址
 *
 */
@Data
@TableName("t_channel_blockchain")
public class ChannelBlockchain extends UUIDEntity {

	private static final long serialVersionUID = 8611350151193561992L;

	/**
	 * 币种名称 BTC ETH USDT
	 */
	private String coin;
	/**
	 * 链名称
	 */
	private String blockchainName;
	/**
	 * 区块链地址图片
	 */
	private String img;

	/**
	 * 区块链地址图片,不带链接
	 */
	@TableField(exist = false)
	private String imgStr;
	/**
	 * 区块链地址
	 */
	private String address;


	private boolean auto = false;



	@TableField(exist = false)
	private String blockchain_name;

}
