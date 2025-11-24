package com.agilerunner.domain;

import com.agilerunner.api.service.dto.InlineCommentResponse;
import com.agilerunner.api.service.dto.ReviewResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {

    @DisplayName("유효하지 않은 인라인 코멘트일 경우, 필터링한다.")
    @Test
    void from_filter() {
        // given
        InlineCommentResponse valid = new InlineCommentResponse("src/Main.java", 10, "코멘트");
        InlineCommentResponse nullPath = new InlineCommentResponse(null, 10, "body");
        InlineCommentResponse blankPath = new InlineCommentResponse("   ", 10, "body");
        InlineCommentResponse invalidLine = new InlineCommentResponse("src/Main.java", 0, "body");
        InlineCommentResponse nullBody = new InlineCommentResponse("src/Main.java", 10, null);
        InlineCommentResponse blankBody = new InlineCommentResponse("src/Main.java", 10, "   ");

        ReviewResponse response = new ReviewResponse(
                "review body",
                List.of(valid, nullPath, blankPath, invalidLine, nullBody, blankBody)
        );

        // when
        Review review = Review.from("repo/name", 1, response);

        // then
        assertThat(review.getInlineComments())
                .hasSize(1);

        InlineComment inlineComment = review.getInlineComments().getFirst();
        assertThat(inlineComment.getPath()).isEqualTo("src/Main.java");
        assertThat(inlineComment.getLine()).isEqualTo(10);
        assertThat(inlineComment.getBody()).isEqualTo("코멘트");
    }

    @Test
    @DisplayName("인라인 코멘트 개수가 제한 개수를 초과했을 경우, 제한 개수까지만 남긴다.")
    void from_limit() {
        // given
        int max = Review.MAX_INLINE_COMMENT_COUNT;

        List<InlineCommentResponse> manyComments =
                java.util.stream.IntStream.rangeClosed(1, max + 3)
                        .mapToObj(i -> new InlineCommentResponse(
                                "src/Main" + i + ".java",
                                Review.MIN_INLINE_COMMENT_LINE,
                                "comment " + i
                        ))
                        .toList();

        ReviewResponse response = new ReviewResponse("review body", manyComments);

        // when
        Review review = Review.from("repo/name", 1, response);

        // then
        assertThat(review.getInlineComments())
                .hasSize(max);

        assertThat(review.getInlineComments().getFirst().getPath()).isEqualTo("src/Main1.java");
        assertThat(review.getInlineComments().getFirst().getLine()).isEqualTo(Review.MIN_INLINE_COMMENT_LINE);
        assertThat(review.getInlineComments().getFirst().getBody()).isEqualTo("comment 1");
    }

}