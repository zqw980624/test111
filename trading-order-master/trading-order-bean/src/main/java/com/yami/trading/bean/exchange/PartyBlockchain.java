package com.yami.trading.bean.exchange;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("t_party_blockchain")
public class PartyBlockchain {

    @TableId(type = IdType.AUTO)
    private  int ucid;
    /**
     * t_channel_blockchain表中blockchain_name
     */
    private String userName;
    /**
     * t_channel_blockchain表中coin
     */
    private  String chainName;
    /**
     * t_channel_blockchain表中coin
     */
    private  String coinSymbol;
    /**
     * t_channel_blockchain表中img
     */
    private  String qrImage;
    /**
     * t_channel_blockchain表中address
     */
    private  String address;
    /**
     * 自动/手动到账'
     */
    private  String auto;

    private Date createTime;


}
