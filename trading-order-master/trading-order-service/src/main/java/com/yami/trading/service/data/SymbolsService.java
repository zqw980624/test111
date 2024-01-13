package com.yami.trading.service.data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.data.domain.Symbols;
import com.yami.trading.dao.data.SymbolsMapper;

/**
 * 币对Service
 * @author lucas
 * @version 2023-03-17
 */
@Service
@Transactional
public class SymbolsService extends ServiceImpl<SymbolsMapper, Symbols> {

}
