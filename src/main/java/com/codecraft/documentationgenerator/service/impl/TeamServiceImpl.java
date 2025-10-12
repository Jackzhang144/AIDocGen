package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.mapper.TeamMapper;
import com.codecraft.documentationgenerator.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 团队服务实现类
 * <p>
 * 提供团队相关的业务逻辑处理
 * 包括团队的创建、查询、更新和删除操作
 *
 * @author CodeCraft
 * @version 1.0
 */
@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据ID查找团队
     *
     * @param id 团队ID
     * @return 团队对象，如果未找到则返回null
     */
    public Team findById(Long id) {
        Team team = teamMapper.findById(id);
        if (team != null) {
            // 解析members JSON字符串
            team.setMembers(parseMembers(team.getMembers()));
        }
        return team;
    }

    /**
     * 根据管理员邮箱查找团队
     *
     * @param admin 管理员邮箱
     * @return 团队对象，如果未找到则返回null
     */
    public Team findByAdmin(String admin) {
        Team team = teamMapper.findByAdmin(admin);
        if (team != null) {
            // 解析members JSON字符串
            team.setMembers(parseMembers(team.getMembers()));
        }
        return team;
    }

    /**
     * 创建新团队
     *
     * @param team 团队对象
     */
    public void createTeam(Team team) {
        // 将members数组转换为JSON字符串
        team.setMembers(serializeMembers(team.getMembers()));
        teamMapper.insert(team);
    }

    /**
     * 更新团队成员
     *
     * @param team 团队对象
     */
    public void updateMembers(Team team) {
        // 将members数组转换为JSON字符串
        team.setMembers(serializeMembers(team.getMembers()));
        teamMapper.updateMembers(team);
    }

    /**
     * 根据ID删除团队
     *
     * @param id 团队ID
     */
    public void deleteById(Long id) {
        teamMapper.deleteById(id);
    }

    /**
     * 查找所有团队
     *
     * @return 团队列表
     */
    public List<Team> findAll() {
        List<Team> teams = teamMapper.findAll();
        // 解析每个team的members JSON字符串
        for (Team team : teams) {
            team.setMembers(parseMembers(team.getMembers()));
        }
        return teams;
    }

    /**
     * 将成员列表序列化为JSON字符串
     *
     * @param members 成员列表JSON字符串
     * @return 序列化的JSON字符串
     */
    private String serializeMembers(String members) {
        // 这里假设members字段已经是JSON格式的字符串
        // 如果需要从String[]转换，可以使用objectMapper.writeValueAsString()
        return members;
    }

    /**
     * 解析JSON字符串为成员列表
     *
     * @param membersJson 成员列表JSON字符串
     * @return 解析后的JSON字符串
     */
    private String parseMembers(String membersJson) {
        // 这里假设直接返回JSON字符串
        // 如果需要转换为String[]，可以使用objectMapper.readValue()
        return membersJson;
    }
}