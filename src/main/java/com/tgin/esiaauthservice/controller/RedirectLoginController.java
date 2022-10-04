package com.tgin.esiaauthservice.controller;

import com.tgin.esiaauthservice.helper.EsiaAuthUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class RedirectLoginController {

    private final EsiaAuthUrlService esiaAuthUrlService;

    @GetMapping("/oauth/esia")
    public String redirectEsiaAuth() {

        String esiaAuthUrl = esiaAuthUrlService.generateAuthCodeUrl();

        return "redirect:" + esiaAuthUrl;
    }
/*
    @GetMapping("/redirectWithRedirectView")
    public RedirectView redirectWithUsingRedirectView(
            RedirectAttributes attributes) {
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
        attributes.addAttribute("attribute", "redirectWithRedirectView");
        return new RedirectView("redirectedUrl");
    }

 */
}