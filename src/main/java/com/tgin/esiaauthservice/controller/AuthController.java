package com.tgin.esiaauthservice.controller;

import com.tgin.esiaauthservice.helper.UrlHelper;
import lombok.RequiredArgsConstructor;

import org.apache.hc.core5.http.NotImplementedException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.TimeZone;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UrlHelper urlHelper;


    @GetMapping(path = "/esia/logout")
    public String logout() throws IOException {
        String url = urlHelper.handleLogout();
        return "redirect:" + url;
    }
    @GetMapping(value = "/esia/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(
            @RequestParam(name = "TimeZone", required = false) TimeZone timeZone
    ) {
        String esiaAuthUrl = urlHelper.handleAuth();
        return "redirect:" + esiaAuthUrl;
    }

    @GetMapping(path = "/esia_return", produces = "text/plain")
    public String handleReturn(
            @RequestParam(name = "code", required = false) String authCode,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) throws IOException {

        return urlHelper.handleReturn(authCode, error, errorDescription);
    }

    @GetMapping(value = "/esia/login/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public String oauthSuccessLogin() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @PostMapping(value = "/esia/login/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public String samlSuccessLogin() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @GetMapping(value = "/esia/logout/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public String samlLogoutSuccess() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @GetMapping(value = "/esia/isLoggedIn", produces = MediaType.APPLICATION_JSON_VALUE)
    public String isLoggedIn() throws NotImplementedException {
        throw new NotImplementedException();
    }

}
