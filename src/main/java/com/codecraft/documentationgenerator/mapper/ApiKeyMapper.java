package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.ApiKey;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApiKeyMapper {
    
    @Select("SELECT * FROM api_keys WHERE id = #{id}")
    ApiKey findById(Long id);
    
    @Select("SELECT * FROM api_keys WHERE hashed_key = #{hashedKey}")
    ApiKey findByHashedKey(String hashedKey);
    
    @Insert("INSERT INTO api_keys(hashed_key, email, purpose) VALUES(#{hashedKey}, #{email}, #{purpose})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ApiKey apiKey);
    
    @Delete("DELETE FROM api_keys WHERE id = #{id}")
    void deleteById(Long id);
    
    @Select("SELECT * FROM api_keys")
    List<ApiKey> findAll();
}