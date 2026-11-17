package com.tecnocampus.LS2.protube_back.infrastructure.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    // In prod the SPA is inside the jar. Its routes (react-router, see frontend/src/App.tsx) must also return
    // index.html, or reloading /login or /register gives 404. Listed one by one on purpose: a catch-all pattern
    // would also catch /assets/*.js and unknown /api/** paths. Add new frontend routes here.
    @GetMapping({"/", "/login", "/register", "/watch/**", "/profile/**", "/upload", "/my-videos"})
    public ModelAndView home() {
        return new ModelAndView("index");
    }
    @PostMapping("logout")
    public ModelAndView logout() {
        return new ModelAndView("logout");
    }

}
