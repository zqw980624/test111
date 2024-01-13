package com.yami.trading.service.syspara;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.yami.trading.bean.syspara.dto.SysparasDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.dao.syspara.SysparaMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置参数Service
 *
 * @author lucas
 * @version 2023-03-17
 */
@Service
@Transactional
public class SysparaService extends ServiceImpl<SysparaMapper, Syspara> {
    /**
     * 通过code 找对象，todo cache
     *
     * @param code
     * @return
     */
    public Syspara find(String code) {
        LambdaQueryWrapper<Syspara> queryWrapper = new LambdaQueryWrapper<Syspara>()
                .eq(Syspara::getCode, code)
                .last("LIMIT 1");
        return super.baseMapper.selectOne(queryWrapper);
    }

    @Transactional(readOnly = false)
    public void updateSysparas(SysparasDto dto) {
        if (dto == null) {
            return;
        }
        Map<String, Object> map = BeanUtil.beanToMap(dto, false, true);
        List<Syspara> updates = map.keySet().stream().map(key -> {
            Syspara syspara = new Syspara();
            syspara.setCode(key);
            syspara.setSvalue(map.get(key).toString());
            return syspara;
        }).collect(Collectors.toList());
        baseMapper.updateBatch(updates);
    }
}
