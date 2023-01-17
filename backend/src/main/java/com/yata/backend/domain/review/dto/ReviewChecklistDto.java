package com.yata.backend.domain.review.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class ReviewChecklistDto {

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    @Builder
    public static class Post {

        @NotNull
        private Long ChecklistID;

        @NotNull
        private boolean checking;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{

        //리뷰 체크 아이디(필요할까?) / 체크 여부
        private Long reviewCheckId;
        private boolean checking;

        //체크리스트 항목에 관한 내용
        private Long ChecklistID;
        private boolean checkpn;

    }
}
