package com.t2g.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "The id provided does not match any existing link")
public class LinkNotFoundException extends RuntimeException {
}
