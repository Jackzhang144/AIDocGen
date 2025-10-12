package com.codecraft.documentationgenerator.service.impl;

import com.codecraft.documentationgenerator.entity.Team;
import com.codecraft.documentationgenerator.exception.BusinessException;
import com.codecraft.documentationgenerator.mapper.TeamMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class TeamServiceImplTest {

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_TeamExists_ReturnsTeam() {
        // Given
        Team expectedTeam = new Team();
        expectedTeam.setId(1L);
        expectedTeam.setAdmin("admin@example.com");
        when(teamMapper.findById(1L)).thenReturn(expectedTeam);

        // When
        Team actualTeam = teamService.findById(1L);

        // Then
        assertNotNull(actualTeam);
        assertEquals(expectedTeam.getId(), actualTeam.getId());
        assertEquals(expectedTeam.getAdmin(), actualTeam.getAdmin());
        verify(teamMapper).findById(1L);
    }

    @Test
    void findById_TeamNotExists_ThrowsBusinessException() {
        // Given
        when(teamMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> teamService.findById(1L));
        assertEquals("团队不存在", exception.getMessage());
        verify(teamMapper).findById(1L);
    }

    @Test
    void findByAdmin_TeamExists_ReturnsTeam() {
        // Given
        Team expectedTeam = new Team();
        expectedTeam.setId(1L);
        expectedTeam.setAdmin("admin@example.com");
        when(teamMapper.findByAdmin("admin@example.com")).thenReturn(expectedTeam);

        // When
        Team actualTeam = teamService.findByAdmin("admin@example.com");

        // Then
        assertNotNull(actualTeam);
        assertEquals(expectedTeam.getAdmin(), actualTeam.getAdmin());
        verify(teamMapper).findByAdmin("admin@example.com");
    }

    @Test
    void findByAdmin_TeamNotExists_ThrowsBusinessException() {
        // Given
        when(teamMapper.findByAdmin("admin@example.com")).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> teamService.findByAdmin("admin@example.com"));
        assertEquals("团队不存在", exception.getMessage());
        verify(teamMapper).findByAdmin("admin@example.com");
    }

    @Test
    void createTeam_ValidTeam_CreatesTeam() {
        // Given
        Team team = new Team();
        team.setAdmin("admin@example.com");

        // When
        teamService.createTeam(team);

        // Then
        verify(teamMapper).insert(any(Team.class));
    }

    @Test
    void deleteById_TeamExists_DeletesTeam() {
        // Given
        Team team = new Team();
        team.setId(1L);
        when(teamMapper.findById(1L)).thenReturn(team);

        // When
        teamService.deleteById(1L);

        // Then
        verify(teamMapper).deleteById(1L);
    }

    @Test
    void deleteById_TeamNotExists_ThrowsBusinessException() {
        // Given
        when(teamMapper.findById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> teamService.deleteById(1L));
        assertEquals("团队不存在", exception.getMessage());
        verify(teamMapper, never()).deleteById(anyLong());
    }

    @Test
    void findAll_ReturnsAllTeams() {
        // Given
        Team team1 = new Team();
        team1.setId(1L);
        team1.setAdmin("admin1@example.com");
        
        Team team2 = new Team();
        team2.setId(2L);
        team2.setAdmin("admin2@example.com");
        
        List<Team> expectedTeams = Arrays.asList(team1, team2);
        when(teamMapper.findAll()).thenReturn(expectedTeams);

        // When
        List<Team> actualTeams = teamService.findAll();

        // Then
        assertNotNull(actualTeams);
        assertEquals(2, actualTeams.size());
        assertEquals(expectedTeams, actualTeams);
        verify(teamMapper).findAll();
    }
}