package com.codecraft.documentationgenerator.service;

import com.codecraft.documentationgenerator.entity.Team;

import java.util.List;

/**
 * 团队服务接口
 * <p>
 * 提供团队相关的业务逻辑处理
 * 包括团队的创建、查询、更新和删除操作
 *
 * @author CodeCraft
 * @version 1.0
 */
public interface TeamService {

    /**
     * 根据ID查找团队
     *
     * @param id 团队ID
     * @return 团队对象，如果未找到则返回null
     */
    Team findById(Long id);

    /**
     * 根据管理员邮箱查找团队
     *
     * @param admin 管理员邮箱
     * @return 团队对象，如果未找到则返回null
     */
    Team findByAdmin(String admin);

    /**
     * 创建新团队
     *
     * @param team 团队对象
     */
    void createTeam(Team team);

    /**
     * 更新团队成员
     *
     * @param team 团队对象
     */
    void updateMembers(Team team);

    /**
     * 根据ID删除团队
     *
     * @param id 团队ID
     */
    void deleteById(Long id);

    /**
     * 查找所有团队
     *
     * @return 团队列表
     */
    List<Team> findAll();
}