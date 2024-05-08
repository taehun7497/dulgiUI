package com.korea.dulgiUI.question;

import com.korea.dulgiUI.answer.Answer;
import com.korea.dulgiUI.answer.AnswerForm;
import com.korea.dulgiUI.answer.AnswerService;
import com.korea.dulgiUI.category.Category;
import com.korea.dulgiUI.category.CategoryService;
import com.korea.dulgiUI.comment.CommentForm;
import com.korea.dulgiUI.uesr.SiteUser;
import com.korea.dulgiUI.uesr.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;

@RequestMapping("/question")
@RequiredArgsConstructor //롬복이 제공하는 애너테이션으로 final이 붙은 속성을 포함하는 생성자를 자동으로 생성하는 역할
@Controller
public class QuestionController {

    // QuestionService, UserService, CategoryService, AnswerService 인스턴스를 주입받습니다.
    private final QuestionService questionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AnswerService answerService;

    // 요청 경로가 "/question/list/{category}"인 HTTP GET 요청을 처리하는 메서드입니다.
    @RequestMapping("/list/{category}")
    public String list(Model model,
                       // 페이지 번호를 나타내는 파라미터로 기본값은 0입니다.
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       // URL 경로에서 추출한 카테고리 이름을 받습니다.
                       @PathVariable("category") String category,
                       // 검색어를 나타내는 파라미터로 기본값은 빈 문자열입니다.
                       @RequestParam(value = "kw", defaultValue = "") String kw) {

        // 카테고리 이름을 기반으로 해당 카테고리를 데이터베이스에서 조회합니다.
        Category category1 = this.categoryService.getCategoryByTitle(category);

        // 특정 페이지에 해당하는 게시물 목록을 검색어와 함께 가져옵니다.
        Page<Question> paging = this.questionService.getList(page, kw, category1);

        // Model 객체는 자바 클래스와 템플릿 간의 연결고리 역할을 한다. Model 객체에 값을 담아두면 템플릿에서 그 값을 사용할 수 있다.
        // Model 객체에 검색 결과, 검색어, 카테고리 정보를 담아서 뷰로 전달합니다.
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);

        // 뷰 이름인 "question_list"를 반환합니다.
        return "question_list";
    }

    @RequestMapping(value = "/detail/{id}")
    public String detail(Model model,
                         // 정렬 옵션을 받는 파라미터로, 기본값은 "recent"입니다.
                         @RequestParam(value = "so", defaultValue = "recent") String so,
                         // 페이지 번호를 나타내는 파라미터로, 기본값은 0입니다.
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         // URL 경로에서 추출한 질문의 ID를 받습니다.
                         @PathVariable("id") Integer id,
                         // 답변 폼 객체입니다.
                         AnswerForm answerForm,
                         // 댓글 폼 객체입니다.
                         CommentForm commentForm,
                         // 현재 사용자의 정보를 가진 Principal 객체입니다.
                         Principal principal) {

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Question question = this.questionService.getQuestion(id);

        // 조회 수 증가 (비로그인 & 작성자 제외)
        // 로그인한 사용자가 질문의 작성자가 아닌 경우에만 조회 수를 증가시킵니다.
        if (principal != null && !question.getAuthor().getUsername().equals(principal.getName())) {
            this.questionService.incrementView(question);
        }

        // 주어진 질문에 대한 답변 목록을 가져옵니다.
        Page<Answer> paging = this.answerService.getList(page, question, so);

        // Model 객체에 조회된 질문, 답변 목록, 정렬 옵션을 담아서 뷰로 전달합니다.
        model.addAttribute("paging", paging);
        model.addAttribute("question", question);
        model.addAttribute("so", so);

        // 뷰 이름인 "question_detail"을 반환합니다.
        return "question_detail";
    }

    // 인증된 사용자만 접근할 수 있도록 설정합니다. 로그아웃 상태에서는 로그인 페이지로 리디렉션됩니다.
    @PreAuthorize("isAuthenticated()") //로그아웃 상태에서는 로그인 페이지로 간다.
    @GetMapping("/create/{category}")
    public String questionCreate(Model model,
                                 // URL 경로에서 추출한 카테고리 이름을 받습니다.
                                 @PathVariable("category") String category,
                                 // 질문 폼 객체입니다.
                                 QuestionForm questionForm) {

        // 모델에 카테고리 정보를 추가하여 뷰로 전달합니다.
        model.addAttribute("category", category);

        // 뷰 이름인 "question_form"을 반환합니다.
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근할 수 있도록 설정합니다.
    @PostMapping("/create/{category}")
    public String questionCreate(Model model,
                                 // URL 경로에서 추출한 카테고리 이름을 받습니다.
                                 @PathVariable("category") String category,
                                 // 유효성 검사를 수행할 질문 폼 객체입니다.
                                 @Valid QuestionForm questionForm,
                                 // 유효성 검사 결과를 저장하는 객체입니다.
                                 BindingResult bindingResult,
                                 // 현재 사용자의 정보를 가진 Principal 객체입니다.
                                 Principal principal) {

        //자동으로 QuestionForm의 subject, content에 바인딩된다. (스프링 프레임 워크의 바인딩 기능)
        if (bindingResult.hasErrors()) { // 만약 유효성 검사에서 오류가 발생했다면
            // 모델에 카테고리 정보를 추가하여 다시 질문 작성 폼으로 돌아갑니다.
            model.addAttribute("category", category);
            return "question_form";
        }

        // 현재 로그인한 사용자의 정보를 가져옵니다.
        SiteUser author = this.userService.getUser(principal.getName());
        // 주어진 카테고리 이름에 해당하는 카테고리 정보를 가져옵니다.
        Category category1 = this.categoryService.getCategoryByTitle(category);
        // 질문을 생성하고 데이터베이스에 저장합니다.
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), author, category1);
        // 생성한 질문이 있는 카테고리 페이지로 리다이렉트합니다.
        return String.format("redirect:/question/list/%s", category);
    }

    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근할 수 있도록 설정합니다.
    @GetMapping("/modify/{id}")
    public String questionModify(
            // 질문 폼 객체입니다.
            QuestionForm questionForm,
            // URL 경로에서 추출한 질문의 ID를 받습니다.
            @PathVariable("id") Integer id,
            // 현재 사용자의 정보를 가진 Principal 객체입니다.
            Principal principal) {

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Question question = this.questionService.getQuestion(id);

        // 현재 로그인한 사용자가 질문의 작성자인지 확인합니다.
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            // 작성자가 아닌 경우 400 Bad Request 응답을 반환합니다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }

        // 질문의 제목과 내용을 질문 폼 객체에 설정합니다.
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());

        // 뷰 이름인 "question_form"을 반환합니다.
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근할 수 있도록 설정합니다.
    @PostMapping("/modify/{id}")
    public String questionModify(
            // 유효성 검사를 수행할 질문 폼 객체입니다.
            @Valid QuestionForm questionForm,
            // URL 경로에서 추출한 질문의 ID를 받습니다.
            @PathVariable("id") Integer id,
            // 현재 사용자의 정보를 가진 Principal 객체입니다.
            Principal principal,
            // 유효성 검사 결과를 저장하는 객체입니다.
            BindingResult bindingResult) {

        // 만약 유효성 검사에서 오류가 발생했다면
        if (bindingResult.hasErrors()) {
            // 다시 질문 수정 폼으로 돌아갑니다.
            return "question_form";
        }

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Question question = this.questionService.getQuestion(id);

        // 현재 로그인한 사용자가 질문의 작성자인지 확인합니다.
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            // 작성자가 아닌 경우 400 Bad Request 응답을 반환합니다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        // 질문을 수정하고 데이터베이스에 반영합니다.
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());

        // 수정된 질문의 세부 정보를 보여주는 페이지로 리다이렉트합니다.
        return String.format("redirect:/question/detail/%s", id);
    }

    // 인증된 사용자만 접근할 수 있도록 설정합니다. 로그아웃 상태에서는 로그인 페이지로 리디렉션됩니다.
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(
            // 현재 사용자의 정보를 가진 Principal 객체입니다.
            Principal principal,
            // URL 경로에서 추출한 질문의 ID를 받습니다.
            @PathVariable("id") Integer id) {

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Question question = this.questionService.getQuestion(id);

        // 현재 로그인한 사용자가 질문의 작성자인지 확인합니다.
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            // 작성자가 아닌 경우 400 Bad Request 응답을 반환합니다.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }

        // 질문을 삭제합니다.
        this.questionService.delete(question);

        // 홈 페이지로 리다이렉트합니다.
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근할 수 있도록 설정합니다.
    @GetMapping("/vote/{id}")
    public String questionVote(
            // 현재 사용자의 정보를 가진 Principal 객체입니다.
            Principal principal,
            // URL 경로에서 추출한 질문의 ID를 받습니다.
            @PathVariable("id") Integer id,
            // HTTP 응답 객체입니다.
            HttpServletResponse response) throws IOException {

        // 주어진 ID에 해당하는 질문을 데이터베이스에서 조회합니다.
        Question question = this.questionService.getQuestion(id);

        // 현재 로그인한 사용자가 질문의 작성자가 아닌 경우에만 실행합니다.
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            // 현재 로그인한 사용자의 정보를 가져옵니다.
            SiteUser siteUser = this.userService.getUser(principal.getName());
            // 질문에 대한 투표를 처리합니다.
            this.questionService.vote(question, siteUser);
        }
        // 질문의 상세 페이지로 리다이렉트합니다.
        return String.format("redirect:/question/detail/%s", id);
    }
}