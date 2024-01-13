package com.yami.trading.bean.data.domain;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * k线图数据Entity
 * @author lucas
 * @version 2023-03-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_kline")
public class Kline extends UUIDEntity implements Comparable<Kline>, Cloneable {
	public final static String PERIOD_1MIN = "1min";
	public final static String PERIOD_5MIN = "5min";
	public final static String PERIOD_15MIN = "15min";
	public final static String PERIOD_30MIN = "30min";
	public final static String PERIOD_60MIN = "60min";
	public final static String PERIOD_2HOUR = "120min";
	public final static String PERIOD_4HOUR = "4hour";
	public final static String PERIOD_1DAY = "1day";
	public final static String PERIOD_5DAY = "5day";

	public final static String PERIOD_1MON = "1mon";
	public final static String PERIOD_1WEEK = "1week";
	public final static String PERIOD_QUARTER = "quarter";
	public final static String PERIOD_YEAR = "year";

	private static final long serialVersionUID = 1L;
	/**
     * SYMBOL
     */
	private String symbol;
	/**
     * TS
     */
	private Long ts;
	/**
     * OPEN
     */
	private BigDecimal open;
	/**
     * HIGH
     */
	private BigDecimal high;
	/**
     * LOW
     */
	private BigDecimal low;
	/**
     * CLOSE
     */
	private BigDecimal close;
	/**
     * AMOUNT
     */
	private BigDecimal amount;
	/**
     * VOLUME
     */
	private BigDecimal volume;
	/**
     * PERIOD
     */
	private String period;
	/**
	 * 时间戳的"yyyy-MM-dd HH:mm:ss"格式
	 */
	@TableField(exist = false)
	private String currentTime;
	@Override
	public int compareTo(Kline kline) {
		if (this.ts > kline.getTs()) {
			return 1;
		} else if (this.ts < kline.getTs()) {
			return -1;
		}
		return 0;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	public void setCurrentTime(String current_time) {
		this.currentTime = current_time;
	}

	public double getAverage(){
		if(high == null || close == null || low == null || open == null){
			return 0;
		}
		return (high.doubleValue() +close.doubleValue() + low.doubleValue()+open.doubleValue())/4.0;
	}
}
