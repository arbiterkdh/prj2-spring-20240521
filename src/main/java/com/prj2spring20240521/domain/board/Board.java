package com.prj2spring20240521.domain.board;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Board {
    private Integer id;
    private String title;
    private String content;
    private String writer; // 작성자 nickName
    private Integer memberId;
    private Integer views;
    private LocalDateTime inserted;

    private Integer numberOfComments;
    private Integer numberOfLike;
    private Integer numberOfImages;
    private List<BoardFile> fileList;
}
