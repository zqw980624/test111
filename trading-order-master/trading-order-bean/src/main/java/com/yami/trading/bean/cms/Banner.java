package com.yami.trading.bean.cms;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;


@Data
@TableName("t_banner")
public class Banner extends UUIDEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8252272898082376329L;

	/**
	 * 语言
	 */
	private String language;
	/**
	 * 语言
	 */
	@TableField(exist = false)
	private String languageText;



	/**
	 * 业务代码， 同种内容 不同语言下的code相同
	 */
	private String contentCode;
	/**
	 * 展示图片
	 */
	private String image;

	@TableField(exist = false)
	private String httpImageUrl;
	/**
	 * 访问路径
	 */
	private String url;
	/**
	 * 是否展示
	 */
	private int onShow;
	/**
	 * 排列顺序（数字相同按时间排，越小排越前）
	 */
	private int sortIndex;
	/**
	 * 类型，top:顶部展示，other:其他地方展示,poster:弹窗海报
	 */
	private String model;
	/**
	 * 是否可以点击跳转
	 */
	private int click;
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;

}
