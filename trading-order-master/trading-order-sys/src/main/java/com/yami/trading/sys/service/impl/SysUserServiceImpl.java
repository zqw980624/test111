package com.yami.trading.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.GoogleAuthenticator;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.sys.dao.SysUserMapper;
import com.yami.trading.sys.dao.SysUserRoleMapper;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
/**
 * 系统用户
 * @author lgh
 */
@Service("sysUserService")
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

	private SysUserRoleMapper sysUserRoleMapper;

	private SysUserMapper sysUserMapper;
	@Autowired
	private SysparaService sysparaService;

	@Autowired
	PasswordManager passwordManager;

	@Autowired
	PasswordEncoder passwordEncoder;
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUserAndUserRole(SysUser user) {
		user.setCreateTime(new Date());
		sysUserMapper.insert(user);
		if(CollUtil.isEmpty(user.getRoleIdList())){
			return ;
		}
		//保存用户与角色关系
		sysUserRoleMapper.insertUserAndUserRole(user.getUserId(), user.getRoleIdList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserAndUserRole(SysUser user) {
		// 更新用户
		sysUserMapper.updateById(user);

		//先删除用户与角色关系
		sysUserRoleMapper.deleteByUserId(user.getUserId());

		if(CollUtil.isEmpty(user.getRoleIdList())){
			return ;
		}
		//保存用户与角色关系
		sysUserRoleMapper.insertUserAndUserRole(user.getUserId(), user.getRoleIdList());
	}

	@Override
	public void updatePasswordByUserId(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setPassword(newPassword);
        user.setUserId(userId);
        sysUserMapper.updateById(user);
    }

	@Override
	public void deleteBatch(Long[] userIds,Long shopId) {
		sysUserMapper.deleteBatch(userIds,shopId);
	}

	@Override
	public SysUser getByUserName(String username) {
		return sysUserMapper.selectByUsername(username);
	}

	@Override
	public SysUser getSysUserById(Long userId) {
		return sysUserMapper.selectById(userId);
	}

	@Override
	public List<String> queryAllPerms(Long userId) {
		return sysUserMapper.queryAllPerms(userId);
	}

	@Override
	public boolean checkGooleAuthCode(long code) {
		long t = System.currentTimeMillis();
	 	 SysUser sysUser= getById(SecurityUtils.getSysUser().getUserId());
		GoogleAuthenticator ga = new GoogleAuthenticator();
		ga.setWindowSize(5);
		boolean flag = ga.check_code(sysUser.getGoogleAuthSecret(),code,t);
		return flag;
	}

	@Override
	public void checkSuperGoogleAuthCode(String code) {
		String secret = sysparaService.find("super_google_auth_secret").getSvalue();
		if (StringUtils.isEmpty(code)) {
			throw new YamiShopBindException("验证码不能为空");
		}
		long t = System.currentTimeMillis();
		GoogleAuthenticator ga = new GoogleAuthenticator();
		ga.setWindowSize(5); // should give 5 * 30 seconds of grace...
		boolean checkCode = ga.check_code(secret, Long.valueOf(code), t);
		if (!checkCode) {
			throw new YamiShopBindException("超级管理员谷歌验证码错误");
		}
	}

	@Override
	public boolean checkSafeWord(String safeword) {
		SysUser sysUser= getById(SecurityUtils.getSysUser().getUserId());
		safeword=passwordManager.decryptPassword(safeword);
		if (StrUtil.isEmpty(sysUser.getSafePassword())){
			throw new YamiShopBindException("资金密码未设置!");
		}
		if (StrUtil.isEmpty(safeword)){
			throw new YamiShopBindException("资金密码不正确!");
		}
		/*System.out.printf(safeword+"=="+passwordEncoder+"======"+sysUser.getSafePassword());
		if (!passwordEncoder.matches(safeword, sysUser.getSafePassword())) {
			throw new YamiShopBindException("资金密码不正确!");
		}*/
		return true;
	}
}
