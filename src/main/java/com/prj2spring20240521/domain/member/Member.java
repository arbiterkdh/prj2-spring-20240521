package com.prj2spring20240521.domain.member;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Member {
    private Integer id;
    private String email;
    private String password;
    private String nickName;
    private LocalDateTime inserted;
}
