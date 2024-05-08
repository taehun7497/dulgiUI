package com.korea.dulgiUI.question;

import com.korea.dulgiUI.answer.Answer;
import com.korea.dulgiUI.category.Category;
import com.korea.dulgiUI.comment.Comment;
import com.korea.dulgiUI.uesr.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity // JPA 엔티티임을 나타냅니다.
@DynamicInsert // INSERT 시에 null인 필드를 제외하고 실행합니다.
public class Question {
    @Id // Primary Key임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성되는 값임을 나타냅니다. (MySQL의 AUTO_INCREMENT와 유사)
    private Integer id; // 질문의 고유 식별자입니다.

    @ManyToOne // 다대일(N:1) 관계를 나타냅니다. 하나의 카테고리는 여러 질문에 속할 수 있습니다.
    private Category category; // 질문이 속한 카테고리입니다.

    @ManyToOne // 다대일(N:1) 관계를 나타냅니다. 하나의 작성자는 여러 질문을 작성할 수 있습니다.
    private SiteUser author; // 질문을 작성한 사용자입니다.

    @Column(length = 200) // 컬럼의 길이를 지정합니다.
    private String subject; // 질문의 제목입니다.

    @Column(columnDefinition = "TEXT") // 컬럼의 데이터 타입을 지정합니다.
    private String content; // 질문의 내용입니다.

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) // 일대다(1:N) 관계를 나타냅니다. 하나의 질문에는 여러 개의 답변이 올 수 있습니다.
    private List<Answer> answerList; // 질문에 달린 답변 목록입니다.

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) // 일대다(1:N) 관계를 나타냅니다. 하나의 질문에는 여러 개의 댓글이 올 수 있습니다.
    private List<Comment> commentList; // 질문에 달린 댓글 목록입니다.

    @ManyToMany // 다대다(N:M) 관계를 나타냅니다. 하나의 질문에 여러 사용자가 투표할 수 있습니다.
    Set<SiteUser> voter; // 질문에 투표한 사용자 목록입니다.

    @Column(columnDefinition = "integer default 0", nullable = false) // 기본값과 null 허용 여부를 지정합니다.
    private int view; // 질문의 조회수입니다.

    private LocalDateTime createDate; // 질문의 작성일시입니다.

    private LocalDateTime modifyDate; // 질문의 수정일시입니다.
}