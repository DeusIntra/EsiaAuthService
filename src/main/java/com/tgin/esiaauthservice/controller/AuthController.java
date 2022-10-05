package com.tgin.esiaauthservice.controller;

import com.tgin.esiaauthservice.helper.UrlHelper;
import lombok.RequiredArgsConstructor;

import org.apache.hc.core5.http.NotImplementedException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.util.TimeZone;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UrlHelper urlHelper;

    @GetMapping(path = "/esia/logout")
    public RedirectView logout() throws IOException {
        String logoutUrl = urlHelper.getLogoutUrl();
        return new RedirectView(logoutUrl); //"redirect:" + url;
    }

    //Редиректит на return_url с параметрами code (авторизационный код) и state
    @GetMapping(value = "/esia/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public RedirectView login(
            @RequestParam(name = "TimeZone", required = false) TimeZone timeZone
    ) {
        String loginUrl = urlHelper.getLoginUrl();
        return new RedirectView(loginUrl); //"redirect:" + esiaAuthUrl;
    }

    @GetMapping(path = "/esia_return", produces = "text/plain")
    public String handleReturn(
            @RequestParam(name = "code", required = false) String authCode,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) {
        boolean isLoggedIn = error == null;
        urlHelper.codeCached = authCode;
        return "code:" + authCode + "\n\nstate:" + state + "\n\nsecret:" + urlHelper.secretCached + "\n\nerror:" + error + "\n\ndescription:" + errorDescription + "\n\nloggedIn:"+ isLoggedIn;//urlHelper.handleReturn(authCode, state, error, errorDescription);
    }

    @GetMapping(path = "/esia/isLoggedIn", produces = "application/json")
    public String isLoggedIn(
            @RequestParam(name = "secret", required = false) String clientSecret
    ) throws IOException {

        String loggedIn = urlHelper.isLoggedIn(clientSecret);
        return loggedIn;
    }

    @GetMapping(value = "/esia/login/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public String oauthSuccessLogin(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) {
        return code + "\n\n" + state;
    }

    @PostMapping(value = "/esia/login/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public String samlSuccessLogin() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @GetMapping(value = "/esia/logout/success", produces = MediaType.APPLICATION_JSON_VALUE)
    public String samlLogoutSuccess() throws NotImplementedException {
        throw new NotImplementedException();
    }

}
