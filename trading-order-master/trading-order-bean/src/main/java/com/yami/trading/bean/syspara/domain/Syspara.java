package com.yami.trading.bean.syspara.domain;

import com.aliyuncs.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 配置参数Entity
 * @author lucas
 * @version 2023-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_syspara")
public class Syspara extends UUIDEntity {

	private static final long serialVersionUID = 1L;
	/**
     * CODE
     */
	private String code;
	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * Ssvalue
     */
	private String svalue;
	/**
     * SORDER
     */
	private Long sorder;
	/**
     * STYPE
     */
	private Long stype;
	/**
     * NOTES
     */
	private String notes;
	/**
     * 0/可修改；1/不可修改；
     */
	private Long modify;
	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常
	 * </p>
	 *
	 * @return
	 */
	public Integer getInteger() {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}

		try {
			return Integer.parseInt(svalue);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常
	 * </p>
	 *
	 * @return
	 */
	public Long getLong() {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}
		try {
			return Long.parseLong(svalue);
		} catch (Exception ex) {
			return null;
		}

	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常
	 * </p>
	 *
	 * @return
	 */
	public Double getDouble() {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}
		try {
			return Double.parseDouble(svalue);
		} catch (Exception ex) {
			return null;
		}

	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常
	 * </p>
	 *
	 * @return
	 */
	public Float getFloat() {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}

		try {
			return Float.parseFloat(svalue);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常
	 * </p>
	 *
	 * @return
	 */
	public BigDecimal getBigDecimal() {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}

		try {
			return new BigDecimal(svalue);
		} catch (Exception ex) {
			return null;
		}

	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常<BR />
	 * 3.默认格式：yyyy-MM-dd
	 * </p>
	 *
	 * @return "yyyy-MM-dd"
	 * @throws ParseException
	 */
	public Date getDate() {
		return getDate("yyyy-MM-dd");
	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常<BR />
	 * 3.按传入的日期格式，将svalue转化成date
	 * </p>
	 *
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public Date getDate(String pattern) {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}
		try {
			return (new SimpleDateFormat(pattern)).parse(svalue);
		} catch (Exception ex) {
			return null;
		}

	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常<BR />
	 * 3.按指定的enum的类型转化
	 * </p>
	 *
	 * @param str
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public Enum<?> getEnum(String enumType) {
		try {
			return getEnum(Class.forName(enumType).asSubclass(Enum.class));
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * <p>
	 * Description: 1.空值或者空字串返回null<BR />
	 * 2.如果转换失败，直接抛异常<BR />
	 * 3.按指定的enum的类型转化
	 * </p>
	 *
	 * @param enumType
	 * @return
	 */
	public <T extends Enum<T>> T getEnum(Class<T> enumType) {
		if (StringUtils.isEmpty(svalue)) {
			return null;
		}

		return T.valueOf(enumType, svalue);
	}



	/**
	 *
	 * <p>
	 * Description: 获取boolean值，会将参数值转化为boolean值
	 * </p>
	 *
	 * @return true or false
	 */
	public boolean getBoolean() {
		if ("Y".equalsIgnoreCase(svalue) || "true".equalsIgnoreCase(svalue) || "1".equals(svalue)) {
			return true;
		} else {
			return false;
		}
	}
}
