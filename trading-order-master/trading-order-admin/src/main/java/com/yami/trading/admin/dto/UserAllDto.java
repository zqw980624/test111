package com.yami.trading.admin.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.Map;

@Data
public class UserAllDto {

    private Page list;

    private  Map  sumData;
}
