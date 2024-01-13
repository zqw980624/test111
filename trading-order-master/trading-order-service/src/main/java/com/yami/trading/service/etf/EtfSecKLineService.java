package com.yami.trading.service.etf;

import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Etf秒级k线图 服务类
 * </p>
 *
 * @author HT
 * @since 2023-05-18 17:27:13
 */
public interface EtfSecKLineService extends IService<EtfSecKLine> {

    void deleteOverdueEtfKLine();


}
