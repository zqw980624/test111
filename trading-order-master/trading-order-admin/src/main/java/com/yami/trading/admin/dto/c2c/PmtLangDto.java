package com.yami.trading.admin.dto.c2c;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel
public class PmtLangDto {



    @ApiModelProperty("语言说明")
    private  String languageIntro;

    @ApiModelProperty("支付方式说明")
    private  String methodTypeIntro;


    private   List<PmtLangDto.TransTypeNameDto> methodTypeList;

    @Data
    public static   class  TransTypeNameDto{

        @ApiModelProperty("类型 id")
        private String typeId;


        private  String transTypeId;


        @ApiModelProperty(":")
        private  String name;

        private  String trans;
    }

}
