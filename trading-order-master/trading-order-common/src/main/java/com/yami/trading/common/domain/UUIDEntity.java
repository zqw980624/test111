package com.yami.trading.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.util.ClassUtils;

import java.io.Serializable;

@Data
public class UUIDEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 实体主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String uuid;

}
