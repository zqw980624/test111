package com.yami.trading.bean.model;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

/**
 * 钱包
 *
 */
@Data
@TableName("tz_wallet")
@Slf4j
public class Wallet extends BaseEntity {

	private static final long serialVersionUID = 7522745589282180818L;

	/**
	 * 关联 party entity
	 */
	private String userId;

	@TableId(type = IdType.ASSIGN_UUID)
	private String uuid;
	/**
	 * 现金
	 */
	private BigDecimal money =new BigDecimal(0);
	
	/**
		 * 锁定金额
	 */
	private BigDecimal lockMoney =new BigDecimal(0);
	
	/**
	 * 冻结金额
	 */
	private BigDecimal freezeMoney =new BigDecimal(0);

	@Version
	private  int version;

	public void setMoney(BigDecimal money) {
		//log.info(money+"===================");
		this.money = money;
	}
}
