package com.tecnocampus.LS2.protube_back.domain.model;

import com.tecnocampus.LS2.protube_back.domain.exception.InvalidCommentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    private static final User ANA = new User(1L, "ana@mail.com", "ana", "hash");

    @Test
    void writtenCommentIsTrimmedAndBelongsToItsAuthor() {
        Comment comment = Comment.write(5L, ANA, "  Nice video \n");

        assertNull(comment.id());
        assertEquals(5L, comment.videoId());
        assertEquals(1L, comment.authorId());
        assertEquals("ana", comment.authorName());
        assertEquals("Nice video", comment.text());
        assertNotNull(comment.createdAt());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  \n\t "})
    void blankTextIsRejected(String text) {
        assertThrows(InvalidCommentException.class, () -> Comment.write(5L, ANA, text));
    }

    @Test
    void textLengthIsLimited() {
        String longest = "a".repeat(Comment.MAX_TEXT_LENGTH);

        assertEquals(longest, Comment.write(5L, ANA, " " + longest + " ").text());
        assertThrows(InvalidCommentException.class, () -> Comment.write(5L, ANA, longest + "a"));
    }

    @Test
    void importedCommentHasNoUserAndKeepsItsDate() {
        Comment comment = Comment.imported(5L, "someone", " Hello ", Instant.EPOCH);

        assertNull(comment.authorId());
        assertEquals("someone", comment.authorName());
        assertEquals("Hello", comment.text());
        assertEquals(Instant.EPOCH, comment.createdAt());
    }

    @Test
    void importedTextThatIsTooLongIsCutWithoutBreakingAnEmoji() {
        String plain = "a".repeat(Comment.MAX_TEXT_LENGTH + 10);
        String beforeEmoji = "a".repeat(Comment.MAX_TEXT_LENGTH - 1);

        assertEquals(Comment.MAX_TEXT_LENGTH, Comment.imported(5L, "x", plain, Instant.EPOCH).text().length());
        assertEquals(beforeEmoji, Comment.imported(5L, "x", beforeEmoji + "😀", Instant.EPOCH).text());
    }
}
