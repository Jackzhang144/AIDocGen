package com.codecraft.documentationgenerator.mapper;

import com.codecraft.documentationgenerator.entity.Doc;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文档Mapper接口
 * <p>
 * 定义文档数据访问接口，用于操作docs表
 *
 * @author CodeCraft
 * @version 1.0
 */
@Mapper
public interface DocMapper {

    /**
     * 根据ID查找文档
     *
     * @param id 文档ID
     * @return Doc 文档对象
     */
    @Select("SELECT * FROM docs WHERE id = #{id}")
    Doc findById(Long id);

    /**
     * 根据用户ID查找文档列表
     *
     * @param userId 用户ID
     * @return List<Doc> 文档列表
     */
    @Select("SELECT * FROM docs WHERE user_id = #{userId}")
    List<Doc> findByUserId(Long userId);

    /**
     * 根据反馈ID查找文档
     *
     * @param feedbackId 反馈ID
     * @return Doc 文档对象
     */
    @Select("SELECT * FROM docs WHERE feedback_id = #{feedbackId}")
    Doc findByFeedbackId(String feedbackId);

    /**
     * 插入新的文档
     *
     * @param doc 文档对象
     */
    @Insert("INSERT INTO docs(user_id, email, output, prompt, language, time_to_generate, time_to_call, " +
            "source, feedback_id, feedback, is_preview, has_accepted_preview, is_explained, doc_format, " +
            "comment_format, kind, is_selection, prompt_id, actual_language, timestamp) " +
            "VALUES(#{userId}, #{email}, #{output}, #{prompt}, #{language}, #{timeToGenerate}, #{timeToCall}, " +
            "#{source}, #{feedbackId}, #{feedback}, #{isPreview}, #{hasAcceptedPreview}, #{isExplained}, #{docFormat}, " +
            "#{commentFormat}, #{kind}, #{isSelection}, #{promptId}, #{actualLanguage}, #{timestamp})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Doc doc);

    /**
     * 更新文档反馈
     *
     * @param doc 文档对象
     */
    @Update("UPDATE docs SET feedback = #{feedback} WHERE feedback_id = #{feedbackId}")
    void updateFeedback(Doc doc);

    /**
     * 根据ID删除文档
     *
     * @param id 文档ID
     */
    @Delete("DELETE FROM docs WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查找所有文档
     *
     * @return List<Doc> 所有文档列表
     */
    @Select("SELECT * FROM docs")
    List<Doc> findAll();
}