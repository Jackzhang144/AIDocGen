package com.codecraft.aidoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.codecraft.aidoc.exception.BusinessException;
import com.codecraft.aidoc.mapper.AiProviderConfigMapper;
import com.codecraft.aidoc.pojo.entity.AiProviderConfigEntity;
import com.codecraft.aidoc.service.AiProviderConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiProviderConfigServiceImpl implements AiProviderConfigService {

    private final AiProviderConfigMapper mapper;

    @Override
    public List<AiProviderConfigEntity> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiProviderConfigEntity saveOrUpdate(AiProviderConfigEntity entity) {
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return mapper.selectById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id) {
        AiProviderConfigEntity target = mapper.selectById(id);
        if (target == null) {
            throw new BusinessException(com.codecraft.aidoc.enums.ErrorCode.RESOURCE_NOT_FOUND, "Provider config not found: " + id);
        }
        // disable all
        LambdaUpdateWrapper<AiProviderConfigEntity> disable = new LambdaUpdateWrapper<>();
        disable.set(AiProviderConfigEntity::getEnabled, false);
        mapper.update(null, disable);

        target.setEnabled(true);
        mapper.updateById(target);
    }

    @Override
    public Optional<AiProviderConfigEntity> findActive() {
        LambdaQueryWrapper<AiProviderConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiProviderConfigEntity::getEnabled, true).last("limit 1");
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }
}
