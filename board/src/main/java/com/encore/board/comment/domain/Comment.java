package com.encore.board.comment.domain;

import com.encore.board.post.domain.Active;
import com.encore.board.post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id // pk
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    @Column(nullable = false, length = 300)
    private String contents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default()
    private com.encore.board.post.domain.Active active = Active.valueOf("N");

    @CreationTimestamp
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(columnDefinition = "TIMESTAMP ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id")
////    @JoinColumn(nullable=false, name="author_email", referenceColumnName="email")
//    private Author author;
}
