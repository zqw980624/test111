package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.UserSafewordApply;
import com.yami.trading.bean.user.dto.UserSafewordApplyDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserSafewordApplyService    extends IService<UserSafewordApply> {
      UserSafewordApply  findByUserIdNoPass(String userId,int operate);

      Page<UserSafewordApplyDto> listRecord(Page page, String rolename, String status,
                                            String userCode, String userName, String operate);


     void  examine(String id,String content,int type);

    /**
     * 人工重置  操作类型 operate:	 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
     */
    public void saveApply(String partyId, String idcard_path_front, String idcard_path_back, String idcard_path_hold, String safeword,
                          String safeword_confirm, Integer operate, String remark);

    List<UserSafewordApply> findByUserId(String userId);
    public Map<String, Object> bindOne(UserSafewordApply apply);
}
