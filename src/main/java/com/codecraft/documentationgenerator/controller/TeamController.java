package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 团队相关接口，实现 Mintlify 团队协作能力
 */
@Slf4j
@RestController
@RequestMapping("/team")
public class TeamController {

    private final TeamServiceInterface teamService;
    private final UserServiceInterface userService;

    public TeamController(TeamServiceInterface teamService, UserServiceInterface userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<TeamResponse> getTeam(@RequestParam String email) {
        log.info("Fetching team overview for {}", email);
        Team team = teamService.findByEmail(email);
        if (team == null) {
            log.debug("No team found for {}; returning empty response", email);
            TeamResponse response = new TeamResponse();
            response.setAdmin(email);
            response.setMembers(List.of());
            return ResponseEntity.ok(response);
        }

        List<TeamMember> members = new ArrayList<>();
        for (String memberEmail : team.getMembers()) {
            TeamMember teamMember = new TeamMember();
            teamMember.setEmail(memberEmail);
            teamMember.setInvitePending(userService.findByEmailOrNull(memberEmail) == null);
            members.add(teamMember);
        }

        TeamResponse response = new TeamResponse();
        response.setAdmin(team.getAdmin());
        response.setMembers(members);
        log.debug("Team {} has {} members", team.getAdmin(), members.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteMember(@RequestBody TeamInviteRequest request) {
        log.info("Invite request from {} to {} (createTeam={})",
                request.getFromEmail(), request.getToEmail(), request.getShouldCreateTeam());
        validateInviteRequest(request);

        if (Boolean.TRUE.equals(request.getShouldCreateTeam())) {
            User admin = userService.findByEmailOrNull(request.getFromEmail());
            if (admin == null || admin.getPlan() == null || !"premium".equalsIgnoreCase(admin.getPlan())) {
                throw new BusinessException("You can only invite others on a premium account");
            }

            teamService.inviteMember(request.getFromEmail(), request.getToEmail());
            log.info("Invite processed successfully from {} to {}", request.getFromEmail(), request.getToEmail());
        } else if (request.getUserId() != null) {
            log.info("Invite attempt without premium plan from {}", request.getFromEmail());
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/invite")
    public ResponseEntity<Void> revokeInvite(@RequestBody TeamInviteRequest request) {
        if (request.getFromEmail() == null || request.getToEmail() == null) {
            throw new BusinessException("Missing email input");
        }

        log.info("Revoking invite for {} from team admin {}", request.getToEmail(), request.getFromEmail());
        teamService.removeMember(request.getFromEmail(), request.getToEmail());
        return ResponseEntity.ok().build();
    }

    private void validateInviteRequest(TeamInviteRequest request) {
        log.debug("Validating invite request: from={} to={}", request.getFromEmail(), request.getToEmail());
        if (request.getFromEmail() == null || request.getToEmail() == null || request.getToEmail().isEmpty()) {
            throw new BusinessException("Missing email input");
        }
        if (request.getFromEmail().equalsIgnoreCase(request.getToEmail())) {
            throw new BusinessException("Cannot add yourself");
        }
    }

    @Data
    public static class TeamResponse {
        private String admin;
        private List<TeamMember> members;
    }

    @Data
    public static class TeamMember {
        private String email;
        private boolean invitePending;
    }

    @Data
    public static class TeamInviteRequest {
        private String userId;
        private String fromEmail;
        private String toEmail;
        private Boolean shouldCreateTeam;
    }
}
