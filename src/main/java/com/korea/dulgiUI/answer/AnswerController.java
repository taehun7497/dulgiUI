package com.korea.dulgiUI.answer;


import com.korea.dulgiUI.question.Question;
import com.korea.dulgiUI.question.QuestionService;
import com.korea.dulgiUI.uesr.SiteUser;
import com.korea.dulgiUI.uesr.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/answer")
@RequiredArgsConstructor // 롬복이 제공하는 애너테이션으로 final이 붙은 속성을 포함하는 생성자를 자동으로 생성합니다.
@Controller
public class AnswerController {

    private final QuestionService questionService; // QuestionService 의존성 주입
    private final AnswerService answerService; // AnswerService 의존성 주입
    private final UserService userService; // UserService 의존성 주입

    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근할 수 있도록 보장합니다.
    @PostMapping("/create/{id}") // POST 요청에 대한 매핑 경로를 지정합니다. {id}는 질문의 ID를 나타냅니다.
    public String createAnswer(Model model,
                               @PathVariable("id") Integer id,
                               @Valid AnswerForm answerForm,
                               BindingResult bindingResult,
                               Principal principal) {

        // 주어진 ID에 해당하는 질문을 가져옵니다.
        Question question = this.questionService.getQuestion(id);

        // 현재 사용자(principal)의 정보를 가져옵니다.
        // principal은 스프링 시큐리티가 제공
        SiteUser siteUser = this.userService.getUser(principal.getName());

        // 답변을 저장합니다.
        if (bindingResult.hasErrors()) {
            // 유효성 검사 오류가 있는 경우 해당 질문과 함께 상세 페이지를 다시 표시합니다.
            model.addAttribute("question", question);
            return "question_detail";
        }

        // 답변을 생성하고 저장한 후, 해당 질문의 상세 페이지로 이동합니다.
        Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s",
                answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm,
                               @PathVariable("id") Integer id,
                               Principal principal) {

        // 주어진 ID에 해당하는 답변을 가져옵니다.
        Answer answer = this.answerService.getAnswer(id);

        // 현재 사용자(principal)가 해당 답변의 작성자인지 확인합니다.
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            // 현재 사용자가 해당 답변의 작성자가 아닌 경우 권한이 없다는 예외를 던집니다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 답변 내용을 수정할 수 있는 폼 페이지로 이동합니다.
        answerForm.setContent(answerForm.getContent());
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm,
                               BindingResult bindingResult,
                               @PathVariable("id") Integer id,
                               Principal principal) {

        // 유효성 검사 오류가 있는지 확인합니다.
        if (bindingResult.hasErrors()) {
            // 유효성 검사 오류가 있는 경우 수정 폼 페이지로 다시 이동합니다.
            return "answer_form";
        }

        // 주어진 ID에 해당하는 답변을 가져옵니다.
        Answer answer = this.answerService.getAnswer(id);

        // 현재 사용자(principal)가 해당 답변의 작성자인지 확인합니다.
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            // 현재 사용자가 해당 답변의 작성자가 아닌 경우 권한이 없다는 예외를 던집니다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 답변 내용을 수정하고 저장한 후, 해당 질문의 상세 페이지로 이동합니다.
        this.answerService.modify(answer, answerForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String answerDelete(Principal principal,
                               @PathVariable("id") Integer id) {

        // 주어진 ID에 해당하는 답변을 가져옵니다.
        Answer answer = this.answerService.getAnswer(id);

        // 현재 사용자(principal)가 해당 답변의 작성자인지 확인합니다.
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            // 현재 사용자가 해당 답변의 작성자가 아닌 경우 권한이 없다는 예외를 던집니다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }

        // 답변을 삭제한 후, 해당 질문의 상세 페이지로 이동합니다.
        this.answerService.delete(answer);
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String answerVote(Principal principal,
                             @PathVariable("id") Integer id) {

        // 주어진 ID에 해당하는 답변을 가져옵니다.
        Answer answer = this.answerService.getAnswer(id);

        // 현재 사용자(principal)의 정보를 가져옵니다.
        SiteUser siteUser = this.userService.getUser(principal.getName());

        // 해당 답변에 투표합니다.
        this.answerService.vote(answer, siteUser);

        // 해당 질문의 상세 페이지로 이동합니다.
        return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
    }
}