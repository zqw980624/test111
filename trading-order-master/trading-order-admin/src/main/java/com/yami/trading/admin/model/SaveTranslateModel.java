package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class SaveTranslateModel {
    /**
     * 参数名
     */
    private  String contentName;
    /**
     * 参数名名称
     */
    private  String content;
    /**
     * 多语言
     */
    private  String langTrans;
}
