package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.aop.RequireLogin;
import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队控制器
 * <p>
 * 处理团队相关的HTTP请求
 * 包括团队的创建、查询、更新和删除操作
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamServiceInterface teamService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据ID获取团队信息
     *
     * @param id 团队ID
     * @return 团队对象
     */
    @RequireLogin
    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable Long id) {
        log.info("Fetching team by ID: {}", id);
        return teamService.findById(id);
    }

    /**
     * 根据管理员邮箱获取团队信息
     *
     * @param admin 管理员邮箱
     * @return 团队对象
     */
    @RequireLogin
    @GetMapping("/admin/{admin}")
    public Team getTeamByAdmin(@PathVariable String admin) {
        log.info("Fetching team by admin: {}", admin);
        return teamService.findByAdmin(admin);
    }

    /**
     * 创建新团队
     *
     * @param teamRequest 团队请求对象
     * @throws JsonProcessingException JSON处理异常
     */
    @RequireLogin
    @PostMapping
    public void createTeam(@RequestBody TeamRequest teamRequest) throws JsonProcessingException {
        log.info("Creating new team with admin: {}", teamRequest.getAdmin());
        Team team = new Team();
        team.setAdmin(teamRequest.getAdmin());
        // 将members数组转换为JSON字符串
        String membersJson = objectMapper.writeValueAsString(teamRequest.getMembers());
        team.setMembers(membersJson);
        teamService.createTeam(team);
    }

    /**
     * 更新团队成员
     *
     * @param teamRequest 团队请求对象
     * @throws JsonProcessingException JSON处理异常
     */
    @RequireLogin
    @PutMapping("/members")
    public void updateTeamMembers(@RequestBody TeamRequest teamRequest) throws JsonProcessingException {
        log.info("Updating team members for team ID: {}", teamRequest.getId());
        Team team = new Team();
        team.setId(teamRequest.getId());
        team.setAdmin(teamRequest.getAdmin());
        // 将members数组转换为JSON字符串
        String membersJson = objectMapper.writeValueAsString(teamRequest.getMembers());
        team.setMembers(membersJson);
        teamService.updateMembers(team);
    }

    /**
     * 删除团队
     *
     * @param id 团队ID
     */
    @RequireLogin
    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        log.info("Deleting team with ID: {}", id);
        teamService.deleteById(id);
    }

    /**
     * 获取所有团队
     *
     * @return 团队列表
     */
    @RequireLogin
    @GetMapping
    public List<Team> getAllTeams() {
        log.info("Fetching all teams");
        return teamService.findAll();
    }

    /**
     * 团队请求DTO类
     * <p>
     * 用于处理团队创建和更新的请求数据
     */
    public static class TeamRequest {
        /**
         * 团队ID
         */
        private Long id;

        /**
         * 管理员邮箱
         */
        private String admin;

        /**
         * 成员列表
         */
        private String[] members;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAdmin() {
            return admin;
        }

        public void setAdmin(String admin) {
            this.admin = admin;
        }

        public String[] getMembers() {
            return members;
        }

        public void setMembers(String[] members) {
            this.members = members;
        }
    }
}