package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.Team;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamMapper {

    @Select("SELECT * FROM teams WHERE id = #{id}")
    Team findById(Long id);

    @Select("SELECT * FROM teams WHERE admin = #{admin}")
    Team findByAdmin(String admin);

    @Insert("INSERT INTO teams(admin, members) VALUES(#{admin}, #{members})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Team team);

    @Update("UPDATE teams SET members = #{members} WHERE id = #{id}")
    void updateMembers(Team team);

    @Delete("DELETE FROM teams WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM teams")
    List<Team> findAll();
}