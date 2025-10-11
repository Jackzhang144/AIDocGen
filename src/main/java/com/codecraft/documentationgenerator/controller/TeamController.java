package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.service.TeamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队控制器
 * 
 * 处理团队相关的HTTP请求
 * 包括团队的创建、查询、更新和删除操作
 * 
 * @author CodeCraft
 * @version 1.0
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 根据ID获取团队信息
     * 
     * @param id 团队ID
     * @return 团队对象
     */
    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable Long id) {
        return teamService.findById(id);
    }
    
    /**
     * 根据管理员邮箱获取团队信息
     * 
     * @param admin 管理员邮箱
     * @return 团队对象
     */
    @GetMapping("/admin/{admin}")
    public Team getTeamByAdmin(@PathVariable String admin) {
        return teamService.findByAdmin(admin);
    }
    
    /**
     * 创建新团队
     * 
     * @param teamRequest 团队请求对象
     * @throws JsonProcessingException JSON处理异常
     */
    @PostMapping
    public void createTeam(@RequestBody TeamRequest teamRequest) throws JsonProcessingException {
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
    @PutMapping("/members")
    public void updateTeamMembers(@RequestBody TeamRequest teamRequest) throws JsonProcessingException {
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
    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteById(id);
    }
    
    /**
     * 获取所有团队
     * 
     * @return 团队列表
     */
    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.findAll();
    }
    
    /**
     * 团队请求DTO类
     * 
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