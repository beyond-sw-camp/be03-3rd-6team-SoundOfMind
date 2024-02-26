package com.encore.comment.service;

import com.encore.comment.domain.Comment;
import com.encore.comment.dto.CommentReqDto;
import com.encore.comment.dto.CommentResDto;
import com.encore.comment.repository.CommentRepository;
import com.encore.common.support.ResponseCode;
import com.encore.common.support.SomException;
import com.encore.post.domain.Post;
import com.encore.post.dto.MemberDto;
import com.encore.post.dto.MemberReqDto;
import com.encore.post.dto.PostResDto;
import com.encore.post.feign.admin.AdminInternalClient;
import com.encore.post.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AdminInternalClient adminInternalClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, AdminInternalClient adminInternalClient, ObjectMapper objectMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.adminInternalClient = adminInternalClient;
        this.objectMapper = objectMapper;
    }

    public void create(Long id, CommentReqDto commentReqDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Post post = postRepository.findById(id).orElseThrow(() -> new SomException(ResponseCode.POST_NOT_FOUND));

        Comment comment = Comment.CreateComment(commentReqDto.getComment(), email, post);
        commentRepository.save(comment);

    }

    public List<CommentResDto> findAll(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        List<Comment> comments = commentRepository.findByPost(post);

        List<MemberDto> list = new ArrayList<>();
        if (!comments.isEmpty()) {
            List<String> emailList = comments.stream()
                    .map(Comment::getEmail).collect(Collectors.toList());

            MemberReqDto memberReqDto = new MemberReqDto();
            memberReqDto.setEmailList(emailList);


            //MemberDto memberDto = adminInternalClient.memberList(memberReqDto);
            ResponseEntity<Map<String,Object>> response = adminInternalClient.memberList(memberReqDto);

            try {
                list = objectMapper.readValue(objectMapper.writeValueAsString(response.getBody().get("rankingList")), new TypeReference<List<MemberDto>>() {
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        List<MemberDto> finalList = list;


        return comments.stream().map(c -> CommentResDto.toCommentRestDto(c,finalList)).collect(Collectors.toList());

    }

    public Comment update(Long commentId, CommentReqDto commentReqDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.update(commentReqDto.getComment());
        return commentRepository.save(comment);

    }

    public Comment delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.deleteComment();
        return comment;

    }
}
