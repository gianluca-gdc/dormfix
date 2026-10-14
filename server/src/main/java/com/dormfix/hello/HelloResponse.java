package com.dormfix.hello;

/** JSON body returned by GET /api/hello. Spring turns this record into JSON automatically. */
public record HelloResponse(String message, String serverTime) {
}
