package com.tgin.esiaauthservice.controller;

import com.tgin.esiaauthservice.helper.EsiaAuthUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RedirectLoginController {

    private final EsiaAuthUrlService esiaAuthUrlService;

    @GetMapping("/oauth/esia")
    public String redirectEsiaAuth() {

        String esiaAuthUrl = esiaAuthUrlService.generateAuthCodeUrl();

        return "redirect:" + esiaAuthUrl;
    }


}
