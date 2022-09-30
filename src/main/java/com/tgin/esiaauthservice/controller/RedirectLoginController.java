package com.tgin.esiaauthservice.controller;

import com.tgin.esiaauthservice.helper.EsiaAuthUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.TimeZone;

@Controller
@RequiredArgsConstructor
public class RedirectLoginController {

    private final EsiaAuthUrlService esiaAuthUrlService;

    @GetMapping("/oauth/esia")
    public String redirectEsiaAuth() {

        String esiaAuthUrl = esiaAuthUrlService.generateAuthCodeUrl();

        return "redirect:" + esiaAuthUrl;
    }

    @GetMapping(value = "/esia/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(
            @RequestParam(name = "TimeZone", required = false) TimeZone timeZone
    ) {


        String esiaAuthUrl = esiaAuthUrlService.generateAuthCodeUrl();

        return "redirect:" + esiaAuthUrl;
    }
}
