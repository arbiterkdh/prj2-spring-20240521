package com.prj2spring20240521.service.board;

import com.prj2spring20240521.domain.board.Board;
import com.prj2spring20240521.domain.board.BoardFile;
import com.prj2spring20240521.mapper.board.BoardMapper;
import com.prj2spring20240521.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper mapper;
    private final MemberMapper memberMapper;
    final S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    String bucketName;

    @Value("${image.src.prefix}")
    String imageSrc;

    public void add(Board board, MultipartFile[] files, Authentication authentication) throws Exception {
        board.setMemberId(Integer.valueOf(authentication.getName()));
        // 게시물 저장
        mapper.insert(board);

        if (files != null) {
            for (MultipartFile file : files) {
                // db 에 해당 게시물의 파일 목록 저장
                mapper.insertFileName(board.getId(), file.getOriginalFilename());
                // 실제 파일 저장

                String key = STR."prj2/\{board.getId()}/\{file.getOriginalFilename()}";
                PutObjectRequest objectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build();

                s3Client.putObject(objectRequest,
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));


            }
        }


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
        leftPageNumber = Math.max(rightPageNumber - 9, 1);
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

    public Map<String, Object> get(Integer id) {
        Board board = mapper.selectById(id);
        List<String> fileNames = mapper.selectFileNameByBoardId(id);
        // 버킷객체 URL/{id}/{name}
        List<BoardFile> files = fileNames.stream()
                .map(name -> new BoardFile(name, STR."\{imageSrc}\{id}/\{name}"))
                .toList();

        board.setFileList(files);

        mapper.selectLikeCount(id);

        Map<String, Object> map = new HashMap<>();
        map.put("board", board);

        return map;
    }

    public void remove(Integer id) {
        // file 명 조회
        List<String> fileNames = mapper.selectFileNameByBoardId(id);

        // aws s3 에 있는 file
        for (String fileName : fileNames) {
            String key = STR."prj2/\{id}/\{fileName}";
            DeleteObjectRequest objectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(objectRequest);
        }


        // board_file
        mapper.deleteByBoardId(id);

        mapper.deleteById(id);
    }

    public void edit(Board board, List<String> removeFileList, MultipartFile[] addFileList) throws IOException {
        if (removeFileList != null && removeFileList.size() > 0) {

            for (String fileName : removeFileList) {
                // aws s3 의 파일 삭제
                String key = STR."prj2/\{board.getId()}/\{fileName}";

                DeleteObjectRequest objectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.deleteObject(objectRequest);

                // db records 삭제
                mapper.deleteFileByBoardIdAndName(board.getId(), fileName);
            }
        }

        if (addFileList != null && addFileList.length > 0) {
            List<String> fileNameList = mapper.selectFileNameByBoardId(board.getId());
            for (MultipartFile file : addFileList) {
                String fileName = file.getOriginalFilename();
                if (!fileNameList.contains(fileName)) {
                    // 새 파일이 기존에 없을 때만 db 에 추가
                    mapper.insertFileName(board.getId(), fileName);
                }
                // aws s3 에 파일 생성
                String key = STR."prj2/\{board.getId()}/\{fileName}";

                PutObjectRequest objectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .acl(ObjectCannedACL.PUBLIC_READ)
                        .build();
                s3Client.putObject(objectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            }
        }

        mapper.update(board);
    }

    public boolean hasAccess(Integer id, Authentication authentication) {
        Board board = mapper.selectById(id);

        boolean self = board.getMemberId()
                .equals(Integer.valueOf(authentication.getName()));
        return self;
    }

    public Map<String, Object> like(Map<String, Object> req, Authentication authentication) {
        Integer boardId = (Integer) req.get("boardId");
        Integer memberId = Integer.valueOf(authentication.getName());

        // 이미 했으면
        int count = mapper.deleteLikeByBoardIdAndMemberId(boardId, memberId);

        // 안했으면
        if (count == 0) {
            mapper.insertLikeByBoardIdAndMemberId(boardId, memberId);
        }
        boolean isLiked = count == 1;
        int likeCount = mapper.selectLikeCount(boardId);
        Map<String, Object> map = new HashMap<>();
        map.put("like", isLiked);
        map.put("count", likeCount);
        return map;
    }
}
