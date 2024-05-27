package com.prj2spring20240521.service.board;

import com.prj2spring20240521.domain.board.Board;
import com.prj2spring20240521.mapper.board.BoardMapper;
import com.prj2spring20240521.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper mapper;
    private final MemberMapper memberMapper;

    public void add(Board board, Authentication authentication) {
        board.setMemberId(Integer.valueOf(authentication.getName()));
        mapper.insert(board);
    }

    public boolean validate(Board board) {
        if (board.getTitle() == null || board.getTitle().isEmpty()) {
            return false;
        } else if (board.getContent() == null || board.getContent().isEmpty()) {
            return false;
        }
        return true;
    }

    public Map<String, Object> list(Integer page, String searchType, String keyword) {
        Map pageInfo = new HashMap();
        Integer countAll = mapper.countAllWithSearch(searchType, keyword);


        Integer offset = (page - 1) * 10;
        Integer lastPageNumber = (countAll - 1) / 10 + 1;
        Integer leftPageNumber = (page - 1) / 10 * 10 + 1;
        Integer rightPageNumber = leftPageNumber + 9 < lastPageNumber ? leftPageNumber + 9 : lastPageNumber;
        leftPageNumber = Math.max(rightPageNumber - 9, 0);
        Integer prevPageNumber = leftPageNumber - 1;
        Integer nextPageNumber = rightPageNumber + 1;

        // todo:
        //  rightPageNumber 는 lastPageNumber 보다 크지 않도록
        //  이전, 처음, 다음, 맨끝 버튼 만들기

        pageInfo.put("currentPageNumber", page);
        pageInfo.put("lastPageNumber", lastPageNumber);
        pageInfo.put("leftPageNumber", leftPageNumber);
        pageInfo.put("rightPageNumber", rightPageNumber);
        pageInfo.put("prevPageNumber", prevPageNumber);
        pageInfo.put("nextPageNumber", nextPageNumber);

        return Map.of(
                "pageInfo", pageInfo,
                "boardList", mapper.selectAllPaging(offset, searchType, keyword));
    }

    public Board get(Integer id) {
        return mapper.selectById(id);
    }

    public void remove(Integer id) {
        mapper.deleteById(id);
    }

    public void edit(Board board) {
        mapper.update(board);
    }

    public boolean hasAccess(Integer id, Authentication authentication) {
        Board board = mapper.selectById(id);

        boolean self = board.getMemberId()
                .equals(Integer.valueOf(authentication.getName()));
        return self;
    }
}
