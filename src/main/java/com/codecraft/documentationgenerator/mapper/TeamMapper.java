package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.Team;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 团队Mapper接口
 * <p>
 * 定义团队数据访问接口，用于操作teams表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Mapper
public interface TeamMapper {

    /**
     * 根据ID查找团队
     *
     * @param id 团队ID
     * @return Team 团队对象
     */
    @Select("SELECT * FROM teams WHERE id = #{id}")
    Team findById(Long id);

    /**
     * 根据管理员邮箱查找团队
     *
     * @param admin 管理员邮箱
     * @return Team 团队对象
     */
    @Select("SELECT * FROM teams WHERE admin = #{admin}")
    Team findByAdmin(String admin);

    /**
     * 插入新的团队
     *
     * @param team 团队对象
     */
    @Insert("INSERT INTO teams(admin, members) VALUES(#{admin}, #{members})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Team team);

    /**
     * 更新团队成员
     *
     * @param team 团队对象
     */
    @Update("UPDATE teams SET members = #{members} WHERE id = #{id}")
    void updateMembers(Team team);

    /**
     * 根据ID删除团队
     *
     * @param id 团队ID
     */
    @Delete("DELETE FROM teams WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查找所有团队
     *
     * @return List<Team> 所有团队列表
     */
    @Select("SELECT * FROM teams")
    List<Team> findAll();
}