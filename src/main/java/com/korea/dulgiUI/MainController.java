package com.korea.dulgiUI;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class MainController {
    @RequestMapping("/")
    public String root(Principal principal) {
        if (principal != null) {

            return "redirect:/question/list/qna";
        } else {
            return "redirect:/user/login";
        }
    }
}
