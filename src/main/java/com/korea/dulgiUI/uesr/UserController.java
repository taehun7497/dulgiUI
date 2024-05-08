package com.korea.dulgiUI.uesr;


import com.korea.dulgiUI.Message;
import com.korea.dulgiUI.answer.Answer;
import com.korea.dulgiUI.answer.AnswerService;
import com.korea.dulgiUI.email.EmailService;
import com.korea.dulgiUI.question.Question;
import com.korea.dulgiUI.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String getInfo(Model model,
                          Principal principal) {

        SiteUser user = this.userService.getUser(principal.getName());
        List<Question> questionList = this.questionService.getQuestions(user);
        List<Answer> answerList = this.answerService.getListByAuthor(user);

        model.addAttribute("answers", answerList);
        model.addAttribute("questions", questionList);
        model.addAttribute("user", user);
        return "user_info";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/password")
    public String modifyPassword(PasswordModifyForm passwordModifyForm) {
        return "modify_password_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/password")
    public String modifyPassword(@Valid PasswordModifyForm passwordModifyForm,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 Model model) {

        SiteUser user = this.userService.getUser(principal.getName());

        if (bindingResult.hasErrors()) {
            return "modify_password_form";
        }

        if (!this.userService.isSamePassword(user, passwordModifyForm.getBeforePassword())) {
            bindingResult.rejectValue("beforePassword", "notBeforePassword", "이전 비밀번호와 일치하지 않습니다. ");
            return "modify_password_form";
        }

        if (!passwordModifyForm.getPassword1().equals(passwordModifyForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "modify_password_form";
        }

        try {
            userService.modifyPassword(user, passwordModifyForm.getPassword1());
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("modifyPasswordFailed", e.getMessage());
            return "modify_password_form";
        }

        model.addAttribute("data", new Message("비밀번호 변경 되었습니다.", "/user/info"));
        return "message";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        if(userCreateForm.getAgree() == null){
//            redirectAttributes.addFlashAttribute("error","이용약관에 동의해주세요.");
            model.addAttribute("error", "이용약관에 동의해주세요");
//            return "redirect:/user/signup";
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1(), userCreateForm.getGender());

            return "success";
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
    }

    @GetMapping("/find-account")
    public String findAccount(Model model) {
        model.addAttribute("sendConfirm", false);
        model.addAttribute("error", false);
        return "find_account";
    }

    @PostMapping("/find-account")
    public String findAccount(Model model,
                              @RequestParam(value = "email") String email) {
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String newpassword = passwordGenerator.generateRandomPassword();
        SiteUser siteUser = userService.getUserByEmail(email);
        siteUser.setPassword(passwordEncoder.encode(newpassword));
        userRepository.save(siteUser);
        emailService.sendMail(siteUser.getEmail(),newpassword);

        return "redirect:/user/login";
    }

    @GetMapping("/edit")
    public String update(Principal principal, Model model) {
        try {
            SiteUser user = userService.getUser(principal.getName());
            model.addAttribute("user", user);

            return "user_edit";
        } catch (Exception e) {
            return "redirect:/user/info";
        }
    }

    @PostMapping("/edit")
    public String update(Principal principal, @RequestParam("nickname") String nickname) {
        SiteUser user = userService.getUser(principal.getName());
        if (user.getNickname() == null)
            userService.updateUser(user, user.getUsername());

        if (user.getNickname().equals(nickname)) {
            return "redirect:/";
        } else {
            userService.updateUser(user, nickname);
            return "redirect:/user/info";
        }
    }

    public static class PasswordGenerator {
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";
        private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
        private static final int PASSWORD_LENGTH = 12;
        public String generateRandomPassword() {
            if (PASSWORD_LENGTH < 1) throw new IllegalArgumentException("Password length must be at least 1");

            StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
            Random random = new Random();
            int baseLength = PASSWORD_ALLOW_BASE.length();

            for (int i = 0; i < PASSWORD_LENGTH; i++) {
                int rndCharAt = random.nextInt(baseLength);
                char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
                sb.append(rndChar);
            }
            return sb.toString();
        }
    }
}