package com.dormfix.user;

/** Email and password supplied to the login endpoint. */
public record LoginRequest(String email, String password) { }
