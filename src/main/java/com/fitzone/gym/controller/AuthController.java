package com.fitzone.gym.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/contact")
    public String contact() { return "contact"; }

    @GetMapping("/owner/login")
    public String ownerLogin() { return "owner_login"; }

    @GetMapping("/member/login")
    public String memberLogin() { return "member_login"; }
}