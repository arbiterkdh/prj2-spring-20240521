package com.prj2spring20240521.controller.comment;

import com.prj2spring20240521.domain.Comment;
import com.prj2spring20240521.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("list/{boardId}")
    public List<Comment> listComments(@PathVariable Integer boardId) {
        return service.get(boardId);
    }
}
