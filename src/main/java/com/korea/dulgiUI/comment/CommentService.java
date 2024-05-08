package com.korea.dulgiUI.comment;

import com.korea.dulgiUI.answer.Answer;
import com.korea.dulgiUI.question.Question;
import com.korea.dulgiUI.uesr.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment create(Question question,
                          String content,
                          SiteUser author) {

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setQuestion(question);
        comment.setCreateDate(LocalDateTime.now());
        this.commentRepository.save(comment);
        return comment;
    }

    public Comment create(Answer answer,
                          String content,
                          SiteUser author) {

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setAnswer(answer);
        comment.setCreateDate(LocalDateTime.now());
        this.commentRepository.save(comment);
        return comment;
    }
}