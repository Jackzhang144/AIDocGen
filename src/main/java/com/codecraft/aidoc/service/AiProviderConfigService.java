package com.codecraft.aidoc.service;

import com.codecraft.aidoc.pojo.entity.AiProviderConfigEntity;

import java.util.List;
import java.util.Optional;

public interface AiProviderConfigService {

    List<AiProviderConfigEntity> listAll();

    AiProviderConfigEntity saveOrUpdate(AiProviderConfigEntity entity);

    void activate(Long id);

    Optional<AiProviderConfigEntity> findActive();
}
