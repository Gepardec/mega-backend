package com.gepardec.mega.db.entity.employee;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class CommentEntityTest {

    @Test
    void setMessage_whenSmallMessage_thenSetsSameMessage() {
        // given
        String input = "Hello";

        // when
        CommentEntity resultComment = new CommentEntity();
        resultComment.setMessage(input);

        // then
        assertThat(resultComment.getMessage()).isEqualTo(input);
    }

    @Test
    void setMessage_whenMessageSizeIsLimit_thenSetsSameMessage() {
        // given
        String inputMessage = "a".repeat(CommentEntity.MAX_MESSAGE_LENGTH);

        // when
        CommentEntity resultComment = new CommentEntity();
        resultComment.setMessage(inputMessage);

        // then
        assertThat(resultComment.getMessage()).isEqualTo(inputMessage);
    }

    @Test
    void setMessage_whenLargeMessage_thenSetsShortenedMessage() {
        // given
        String inputMessage = "a".repeat(CommentEntity.MAX_MESSAGE_LENGTH + 10);

        // when
        CommentEntity resultComment = new CommentEntity();
        resultComment.setMessage(inputMessage);

        // then
        String expectedMessage = "a".repeat(CommentEntity.MAX_MESSAGE_LENGTH - 3) + "...";
        assertThat(resultComment.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void setMessage_whenNull_thenMessageNull() {
        CommentEntity messageComment = new CommentEntity();
        messageComment.setMessage("becomes null");

        messageComment.setMessage(null);

        assertThat(messageComment.getMessage()).isNull();
    }
}
