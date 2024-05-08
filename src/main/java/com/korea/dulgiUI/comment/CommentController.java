package com.korea.dulgiUI.comment;

import com.korea.dulgiUI.answer.Answer;
import com.korea.dulgiUI.answer.AnswerService;
import com.korea.dulgiUI.question.Question;
import com.korea.dulgiUI.question.QuestionService;
import com.korea.dulgiUI.uesr.SiteUser;
import com.korea.dulgiUI.uesr.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final QuestionService questionService; // 질문 서비스
    private final AnswerService answerService; // 답변 서비스
    private final CommentService commentService; // 댓글 서비스
    private final UserService userService; // 사용자 서비스

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/question/{id}")
    public String createAboutQuestion(Model model,
                                      @PathVariable("id") Integer id,
                                      @Valid CommentForm commentForm,
                                      BindingResult bindingResult,
                                      Principal principal) {

        Question question = this.questionService.getQuestion(id); // 주어진 ID에 해당하는 질문 가져오기
        SiteUser siteUser = this.userService.getUser(principal.getName()); // 현재 사용자 가져오기

        if (bindingResult.hasErrors()) { // 바인딩 결과에 오류가 있는 경우
            model.addAttribute("question", question); // 모델에 질문 추가
            return "question_detail"; // 질문 상세 페이지로 이동
        }

        Comment comment = this.commentService.create(question, commentForm.getContent(), siteUser); // 댓글 생성
        return String.format("redirect:/question/detail/%s", comment.getQuestion().getId()); // 댓글이 달린 질문의 상세 페이지로 리다이렉트
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/answer/{id}")
    public String createAboutAnswer(Model model,
                                    @PathVariable("id") Integer id,
                                    @RequestParam(value = "so", defaultValue = "recent") String so,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @Valid CommentForm commentForm,
                                    BindingResult bindingResult,
                                    Principal principal) {

        Answer answer = this.answerService.getAnswer(id); // 주어진 ID에 해당하는 답변 가져오기
        SiteUser siteUser = this.userService.getUser(principal.getName()); // 현재 사용자 가져오기
        Question question = answer.getQuestion(); // 답변이 속한 질문 가져오기

        if (bindingResult.hasErrors()) { // 바인딩 결과에 오류가 있는 경우
            model.addAttribute("question", question); // 모델에 질문 추가
            return "question_detail"; // 질문 상세 페이지로 이동
        }

        Comment comment = this.commentService.create(answer, commentForm.getContent(), siteUser); // 댓글 생성
        model.addAttribute("so", so); // 정렬 순서 모델에 추가
        return String.format("redirect:/question/detail/%s?page=%s&so=%s#answer_%s",
                comment.getAnswer().getQuestion().getId(),
                page,
                so,
                comment.getAnswer().getId()); // 댓글이 달린 답변이 속한 질문의 상세 페이지로 리다이렉트
    }
}