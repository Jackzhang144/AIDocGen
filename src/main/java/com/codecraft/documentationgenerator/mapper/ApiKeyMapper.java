package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.ApiKey;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * API密钥Mapper接口
 * <p>
 * 定义API密钥数据访问接口，用于操作api_keys表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Mapper
public interface ApiKeyMapper {

    /**
     * 根据ID查找API密钥
     *
     * @param id API密钥ID
     * @return ApiKey API密钥对象
     */
    @Select("SELECT * FROM api_keys WHERE id = #{id}")
    ApiKey findById(Long id);

    /**
     * 根据哈希值查找API密钥
     *
     * @param hashedKey 哈希后的API密钥
     * @return ApiKey API密钥对象
     */
    @Select("SELECT * FROM api_keys WHERE hashed_key = #{hashedKey}")
    ApiKey findByHashedKey(String hashedKey);

    /**
     * 插入新的API密钥
     *
     * @param apiKey API密钥对象
     */
    @Insert("INSERT INTO api_keys(hashed_key, first_name, last_name, email, purpose, created_at) " +
            "VALUES(#{hashedKey}, #{firstName}, #{lastName}, #{email}, #{purpose}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ApiKey apiKey);

    /**
     * 根据ID删除API密钥
     *
     * @param id API密钥ID
     */
    @Delete("DELETE FROM api_keys WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查找所有API密钥
     *
     * @return List<ApiKey> API密钥列表
     */
    @Select("SELECT * FROM api_keys")
    List<ApiKey> findAll();
}
