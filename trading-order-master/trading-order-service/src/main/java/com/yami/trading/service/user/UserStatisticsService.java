package com.yami.trading.service.user;

import java.util.List;
import java.util.Map;

public interface UserStatisticsService {
    public List<Map<String,Object>> getWalletExtends(String loginPartyId, String targetPartyId) ;

    public List<Map<String,Object>> getAssetsAll(String loginPartyId,String targetPartyId);

}
