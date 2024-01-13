package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.UserBank;
import com.yami.trading.dao.c2c.UserBankMapper;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.c2c.UserBankService;
import org.springframework.stereotype.Service;

@Service
public class UserBankServiceImpl extends ServiceImpl<UserBankMapper, UserBank> implements UserBankService {


}
