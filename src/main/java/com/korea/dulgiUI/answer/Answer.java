package com.korea.dulgiUI.answer;


import com.korea.dulgiUI.comment.Comment;
import com.korea.dulgiUI.question.Question;
import com.korea.dulgiUI.uesr.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity // JPA 엔티티임을 나타냅니다.
public class Answer {
    @Id // Primary Key임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성되는 값임을 나타냅니다. (MySQL의 AUTO_INCREMENT와 유사)
    private Integer id; // 답변의 고유 식별자입니다.

    @Column(columnDefinition = "TEXT") // 컬럼의 데이터 타입을 지정합니다.
    private String content; // 답변의 내용입니다.

    @ManyToOne // 다대일(N:1) 관계를 나타냅니다. 하나의 답변은 하나의 질문에 속합니다.
    private Question question; // 답변이 속한 질문입니다.

    @ManyToOne // 다대일(N:1) 관계를 나타냅니다. 하나의 작성자는 여러 답변을 작성할 수 있습니다.
    private SiteUser author; // 답변을 작성한 사용자입니다.

    @ManyToMany // 다대다(N:M) 관계를 나타냅니다. 하나의 답변에 여러 사용자가 투표할 수 있습니다.
    Set<SiteUser> voter; // 답변에 투표한 사용자 목록입니다.

    @OneToMany(mappedBy = "answer", cascade = CascadeType.REMOVE) // 일대다(1:N) 관계를 나타냅니다. 하나의 답변에는 여러 개의 댓글이 올 수 있습니다.
    private List<Comment> commentList; // 답변에 달린 댓글 목록입니다.

    private LocalDateTime modifyDate; // 답변의 수정일시입니다.

    private LocalDateTime createDate; // 답변의 작성일시입니다.

    private String text;
}