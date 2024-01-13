package com.yami.trading.bean.item.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class InfomationDTO {

    /**
     * 原始链接
     */
    private String id;

    private String time;
    /**
     * 标题
     */
    private String title;

    private String  img;
    /**
     * 带标签正文
     */
    private String content;



}
