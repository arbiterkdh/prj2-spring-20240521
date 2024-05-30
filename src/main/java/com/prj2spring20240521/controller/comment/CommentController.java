package com.prj2spring20240521.controller.comment;

import com.prj2spring20240521.domain.Comment;
import com.prj2spring20240521.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    final CommentService service;

    @PostMapping("add")
    public void addComment(@RequestBody Comment comment,
                           Authentication authentication) {
        System.out.println("comment = " + comment);
        service.add(comment, authentication);
    }
}
