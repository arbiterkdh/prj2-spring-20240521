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

    public List<Comment> get(Integer id) {
        return mapper.selectCommentByBoardId(id);
    }
}