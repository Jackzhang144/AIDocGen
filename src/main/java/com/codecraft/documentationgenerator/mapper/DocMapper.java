package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.Doc;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocMapper {

    @Select("SELECT * FROM docs WHERE id = #{id}")
    Doc findById(Long id);

    @Select("SELECT * FROM docs WHERE user_id = #{userId}")
    List<Doc> findByUserId(Long userId);

    @Select("SELECT * FROM docs WHERE feedback_id = #{feedbackId}")
    Doc findByFeedbackId(String feedbackId);

    @Insert("INSERT INTO docs(user_id, email, output, prompt, language, time_to_generate, time_to_call, " +
            "source, feedback_id, feedback, is_preview, has_accepted_preview, is_explained, doc_format, " +
            "comment_format, kind, is_selection, prompt_id, actual_language, timestamp) " +
            "VALUES(#{userId}, #{email}, #{output}, #{prompt}, #{language}, #{timeToGenerate}, #{timeToCall}, " +
            "#{source}, #{feedbackId}, #{feedback}, #{isPreview}, #{hasAcceptedPreview}, #{isExplained}, #{docFormat}, " +
            "#{commentFormat}, #{kind}, #{isSelection}, #{promptId}, #{actualLanguage}, #{timestamp})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Doc doc);

    @Update("UPDATE docs SET feedback = #{feedback} WHERE feedback_id = #{feedbackId}")
    void updateFeedback(Doc doc);

    @Delete("DELETE FROM docs WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT * FROM docs")
    List<Doc> findAll();
}