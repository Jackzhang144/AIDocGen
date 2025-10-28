package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.constant.MessageConstants;
import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.TeamMapper;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
@Slf4j
@Service
public class TeamServiceImpl implements TeamServiceInterface {

    @Autowired
    private TeamMapper teamMapper;

    /**
     * 根据ID查找团队
     *
     * @param id 团队ID
     * @return 团队对象，如果未找到则返回null
     */
    public Team findById(Long id) {
        log.info("Finding team by ID: {}", id);
        Team team = teamMapper.findById(id);
        if (team == null) {
            throw new BusinessException(MessageConstants.TEAM_NOT_FOUND);
        }
        ensureMembersList(team);
        return team;
    }

    /**
     * 根据管理员邮箱查找团队
     *
     * @param admin 管理员邮箱
     * @return 团队对象，如果未找到则返回null
     */
    public Team findByAdmin(String admin) {
        log.info("Finding team by admin: {}", admin);
        Team team = teamMapper.findByAdmin(admin);
        if (team == null) {
            throw new BusinessException(MessageConstants.TEAM_NOT_FOUND);
        }
        ensureMembersList(team);
        return team;
    }

    public Team findByAdminOrNull(String admin) {
        Team team = teamMapper.findByAdmin(admin);
        ensureMembersList(team);
        return team;
    }

    public Team findByMember(String memberEmail) {
        Team team = teamMapper.findByMember(memberEmail);
        ensureMembersList(team);
        return team;
    }

    public Team findByEmail(String email) {
        Team team = findByAdminOrNull(email);
        if (team != null) {
            return team;
        }
        return findByMember(email);
    }

    /**
     * 创建新团队
     *
     * @param team 团队对象
     */
    public void createTeam(Team team) {
        log.info("Creating new team with admin: {}", team.getAdmin());
        ensureMembersList(team);
        team.setCreatedAt(LocalDateTime.now());
        teamMapper.insert(team);
    }

    /**
     * 更新团队成员
     *
     * @param team 团队对象
     */
    public void updateMembers(Team team) {
        log.info("Updating members for team ID: {}", team.getId());
        ensureMembersList(team);
        teamMapper.updateMembers(team);
    }

    public Team inviteMember(String adminEmail, String memberEmail) {
        log.info("Inviting member {} to team {}", memberEmail, adminEmail);
        Team team = findByAdminOrNull(adminEmail);
        if (team == null) {
            team = new Team();
            team.setAdmin(adminEmail);
            team.setMembers(new ArrayList<>());
            createTeam(team);
        }

        ensureMembersList(team);
        if (team.getMembers().contains(memberEmail)) {
            throw new BusinessException("Member already invited to the team");
        }

        if (team.getMembers().size() >= 2) {
            throw new BusinessException("Cannot have more than 3 members in team");
        }

        team.getMembers().add(memberEmail);
        updateMembers(team);
        return team;
    }

    public void removeMember(String adminEmail, String memberEmail) {
        log.info("Removing member {} from team {}", memberEmail, adminEmail);
        Team team = findByAdminOrNull(adminEmail);
        if (team == null) {
            return;
        }
        ensureMembersList(team);
        team.getMembers().remove(memberEmail);
        updateMembers(team);
    }

    /**
     * 根据ID删除团队
     *
     * @param id 团队ID
     */
    public void deleteById(Long id) {
        log.info("Deleting team with ID: {}", id);
        Team team = teamMapper.findById(id);
        if (team == null) {
            throw new BusinessException(MessageConstants.TEAM_NOT_FOUND);
        }
        teamMapper.deleteById(id);
    }

    /**
     * 查找所有团队
     *
     * @return 团队列表
     */
    public List<Team> findAll() {
        log.info("Finding all teams");
        List<Team> teams = teamMapper.findAll();
        for (Team team : teams) {
            ensureMembersList(team);
        }
        return teams;
    }

    private void ensureMembersList(Team team) {
        if (team == null) {
            return;
        }
        if (team.getMembers() == null) {
            team.setMembers(new ArrayList<>());
        }
    }
}
