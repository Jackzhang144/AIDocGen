package com.codecraft.documentationgenerator.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentFormatterTest {

    @Test
    void wrapShouldRespectWidth() {
        String wrapped = CommentFormatter.wrap("one two three four five", 10);
        assertThat(wrapped).contains("\n");
        assertThat(wrapped).startsWith("one two");
    }

    @Test
    void addCommentsShouldWrapUsingJsDoc() {
        String commented = CommentFormatter.addComments("Adds two numbers", "javascript", "function");
        assertThat(commented).startsWith("/**").contains("Adds two numbers");
    }

    @Test
    void inferCommentFormatShouldDefaultToLine() {
        String format = CommentFormatter.inferCommentFormat("go", "type", "none");
        assertThat(format).isEqualTo("Line");
    }
}
