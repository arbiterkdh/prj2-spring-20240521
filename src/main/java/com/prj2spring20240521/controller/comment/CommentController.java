package com.prj2spring20240521.controller.comment;

import com.prj2spring20240521.domain.Comment;
import com.prj2spring20240521.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    final CommentService service;

    @PostMapping("add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity addComment(@RequestBody Comment comment,
                                     Authentication authentication) {
        if (service.validate(comment)) {
            service.add(comment, authentication);

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("list/{boardId}")
    public List<Comment> list(@PathVariable Integer boardId) {
        return service.list(boardId);
    }

    @DeleteMapping("remove")
    public ResponseEntity remove(@RequestBody Comment comment, Authentication authentication) {
        if (service.hasAccess(comment, authentication)) {
            service.remove(comment);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("modify")
    public ResponseEntity modify(@RequestBody Comment comment, Authentication authentication) {
        if (service.hasAccess(comment, authentication)) {
            service.modify(comment);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
