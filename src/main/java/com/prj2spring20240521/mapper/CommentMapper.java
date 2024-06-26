package com.prj2spring20240521.mapper;

import com.prj2spring20240521.domain.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("""
            INSERT INTO comment
            (board_id, member_id, comment)
            VALUES (#{boardId}, #{memberId}, #{comment})
            """)
    int insert(Comment comment);

    @Select("""
            SELECT c.id , c.board_id, c.member_id, c.comment, c.inserted, m.nick_name nickName
            FROM comment c JOIN member m ON c.member_id = m.id
            WHERE board_id = #{boardId}
            ORDER BY id DESC
            """)
    List<Comment> selectAllByBoardId(Integer boardId);

    @Delete("""
            DELETE
            FROM comment
            WHERE id = #{id}
            """)
    int deleteById(Integer id);

    @Delete("""
            DELETE
            FROM comment
            WHERE member_id = #{memberId}
            """)
    int deleteCommentByMemberId(Integer memberId);

    @Select("""
            SELECT member_id
            FROM comment
            WHERE id = #{id}
            """)
    String selectMemberId(Integer id);

    @Update("""
            UPDATE comment
            SET comment = #{comment},
                inserted = NOW()
            WHERE id = #{id}
            """)
    int update(String comment, Integer id);
}
