package com.codecraft.documentationgenerator.controller;

import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.entity.User;
import com.codecraft.documentationgenerator.exception.GlobalExceptionHandler;
import com.codecraft.documentationgenerator.service.TeamServiceInterface;
import com.codecraft.documentationgenerator.service.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeamControllerTest {

    private TeamServiceInterface teamService;
    private UserServiceInterface userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        teamService = Mockito.mock(TeamServiceInterface.class);
        userService = Mockito.mock(UserServiceInterface.class);

        TeamController controller = new TeamController(teamService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTeam_shouldReturnEmptyStructureWhenNoTeam() throws Exception {
        Mockito.when(teamService.findByEmail("solo@example.com")).thenReturn(null);

        mockMvc.perform(get("/team").param("email", "solo@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value("solo@example.com"))
                .andExpect(jsonPath("$.members", hasSize(0)));
    }

    @Test
    void getTeam_shouldReturnMembers() throws Exception {
        Team team = new Team();
        team.setAdmin("lead@example.com");
        team.setMembers(List.of("member@example.com"));
        Mockito.when(teamService.findByEmail("lead@example.com")).thenReturn(team);
        User member = new User();
        member.setEmail("member@example.com");
        Mockito.when(userService.findByEmailOrNull("member@example.com")).thenReturn(member);

        mockMvc.perform(get("/team").param("email", "lead@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value("lead@example.com"))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0].invitePending").value(false));
    }

    @Test
    void invite_shouldValidatePremiumPlan() throws Exception {
        User admin = new User();
        admin.setPlan("premium");
        Mockito.when(userService.findByEmailOrNull("lead@example.com")).thenReturn(admin);
        Team updated = new Team();
        updated.setAdmin("lead@example.com");
        updated.setMembers(List.of("dev@example.com"));
        Mockito.when(teamService.inviteMember("lead@example.com", "dev@example.com")).thenReturn(updated);

        mockMvc.perform(post("/team/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"uid\",\"fromEmail\":\"lead@example.com\",\"toEmail\":\"dev@example.com\",\"shouldCreateTeam\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void invite_shouldRejectSelfInvite() throws Exception {
        mockMvc.perform(post("/team/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"uid\",\"fromEmail\":\"lead@example.com\",\"toEmail\":\"lead@example.com\",\"shouldCreateTeam\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot add yourself"));
    }

    @Test
    void revoke_shouldCallService() throws Exception {
        mockMvc.perform(delete("/team/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromEmail\":\"lead@example.com\",\"toEmail\":\"dev@example.com\"}"))
                .andExpect(status().isOk());

        Mockito.verify(teamService).removeMember("lead@example.com", "dev@example.com");
    }
}
