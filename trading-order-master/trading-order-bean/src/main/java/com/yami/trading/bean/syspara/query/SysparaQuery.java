package com.yami.trading.bean.syspara.query;

import javax.validation.constraints.NotNull;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 配置参数DTO
 * @author lucas
 * @version 2023-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysparaQuery{

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
    @Query(type = QueryType.EQ)
	private String uuid;
	/**
     * CODE
     */
    @Query(type = QueryType.EQ)
	private String code;

	/**
     * STYPE
     */
    @Query(type = QueryType.EQ)
	private Long stype;

	/**
	 * STYPE
	 */
	@Query(tableColumn = "stype", type = QueryType.NE)
	private Long notStype;

	/**
     * NOTES
     */
    @Query(type = QueryType.LIKE)
	private String notes;
	/**
     * 0/可修改；1/不可修改；
     */
	@NotNull(message="0/可修改；1/不可修改；不能为空")
    @Query(type = QueryType.EQ)
	private Long modify;
   

}
