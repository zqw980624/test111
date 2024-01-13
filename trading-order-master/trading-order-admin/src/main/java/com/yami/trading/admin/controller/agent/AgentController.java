package com.yami.trading.admin.controller.agent;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.service.SysUserOperService;
import com.yami.trading.admin.model.*;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.GoogleAuthenticator;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "代理商")
@RequestMapping("agent")
public class AgentController {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PasswordManager passwordManager;
    @Autowired
    UserService userService;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    AgentService agentService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    LogService logService;
    @Autowired
    SysUserOperService sysUserOperService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<AgentDto>> list(@RequestBody @Valid UserAgentListModel model) {
        List<String> roleNames = new ArrayList<>();
        roleNames.add(Constants.SECURITY_ROLE_AGENT);
        Page<AgentDto> page = new Page(model.getCurrent(), model.getSize());
        if ("list".equals(model.getViewType())) {
            page = agentService.listTotal(page, model.getUserName());
        } else {
            page = agentService.pagedQueryNetwork(page);
        }
        Syspara syspara = sysparaService.find("project_type");
        String shareUrl = sysparaService.find("share_url").getSvalue();
        if (null == syspara) {
            shareUrl = shareUrl + "#/?code=";
        } else {
            // 项目类型：DAPP_EXCHANGE(DAPP+交易所)；EXCHANGE(交易所)；(后面可以拼接项目编号，例如：EXCHANGE_TD1)
            String projectType = syspara.getSvalue();
            if (projectType.contains("DAPP_EXCHANGE")) {
                shareUrl = shareUrl + "#/?code=";
            } else {
                shareUrl = shareUrl + "#/?usercode=";
            }
        }
        //List<AgentDto> list = page.getRecords();
        for (AgentDto agentDto : page.getRecords()) {
            agentDto.setShareUrl(shareUrl + agentDto.getUserCode());
            agentDto.setLoginAuthority(agentDto.getStatus() == 1);
            agentDto.setOperaAuthority(Constants.SECURITY_ROLE_AGENT.equals(agentDto.getRoleName()));
        }
        return Result.ok(page);
    }


    @ApiOperation(value = "新增")
    @PostMapping("add")
    public Result<?> add(@RequestBody @Valid AgentAndModel model) {
        sysUserService.checkSafeWord(model.getSafeword());
        String roleName = model.isOperaAuthority() ? Constants.SECURITY_ROLE_AGENT : Constants.SECURITY_ROLE_AGENTLOW;
        sysUserOperService.addAgent(model.getUserName(),
                passwordManager.decryptPassword(model.getPassword()), "000000", roleName, model.getRemarks(), model.getParentsUseCode(),
                model.isLoginAuthority(), model.isOperaAuthority());
        return Result.ok(null);
    }

    @ApiOperation(value = "修改代理商")
    @PostMapping("update")
    public Result<?> update(@RequestBody @Valid UpdateAgentModel model) {
        agentService.updateAgent(model.getId(), model.isLoginAuthority(), model.isOperaAuthority(), model.getRemarks());
        return Result.ok(null);
    }

    @ApiOperation(value = "重置登录密码")
    @PostMapping("restPassword")
    public Result<?> add(@RequestBody @Valid RestPasswordModel model) {
        sysUserService.checkSafeWord(model.getSafeword());
        sysUserOperService.restPassword(passwordManager.decryptPassword(model.getPassword()),model.getId());
        return Result.ok(null);
    }

    @ApiOperation(value = "绑定谷歌验证码")
    @PostMapping("bindGoogleAuthCode")
    public Result<?> bindGoogleAuthCode(@RequestBody @Valid AgentBindGoogleModel model) {
        sysUserService.checkSuperGoogleAuthCode(model.getRootGoogleCode());
        long t = System.currentTimeMillis();
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5); // should give 5 * 30 seconds of grace...
        sysUserOperService.bindGoogleAuthCode(model.getGoogleAuthSecret(), model.getId());
        return Result.ok(null);
    }

    @ApiOperation(value = "解除绑定谷歌验证码")
    @PostMapping("unbindGoogleAuthCode")
    public Result<?> unbindGoogleAuthCode(@RequestBody @Valid IdModel model) {
        sysUserOperService.unbindGoogleAuthCode( model.getId());
        return Result.ok(null);
    }
}
