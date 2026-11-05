package com.dormfix.user;

/** Public account details returned after a successful login. */
public record LoginResponse(Long userId, String name, Role role, String roomNumber) { }
