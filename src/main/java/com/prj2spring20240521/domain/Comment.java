package com.prj2spring20240521.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer id;
    private Integer boardId;
    private Integer memberId;
    private String comment;
    private LocalDateTime inserted;

//    private String memberNickName;
}
