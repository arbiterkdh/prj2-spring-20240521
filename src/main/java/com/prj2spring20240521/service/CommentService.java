package com.prj2spring20240521.service;

import com.prj2spring20240521.domain.Comment;
import com.prj2spring20240521.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CommentService {

    final CommentMapper mapper;

    public void add(Comment comment, Authentication authentication) {
        comment.setMemberId(Integer.valueOf(authentication.getName()));

        mapper.insert(comment);
    }

    public List<Comment> list(Integer boardId) {
        return mapper.selectAllByBoardId(boardId);
    }

    public boolean validate(Comment comment) {
        if (comment == null) {
            return false;
        }

        if (comment.getComment().isBlank()) {
            return false;
        }

        if (comment.getBoardId() == null) {
            return false;
        }
        return true;
    }

    public void remove(Comment comment) {
        mapper.deleteById(comment.getId());
    }

    public boolean hasAccess(Comment comment, Authentication authentication) {
        if (authentication.getName().equals(mapper.selectMemberId(comment.getId()))) {
            return true;
        }
        return false;
    }

    public void modify(Comment comment) {
        String commentText = comment.getComment();
        mapper.update(commentText, comment.getId());
    }
}
