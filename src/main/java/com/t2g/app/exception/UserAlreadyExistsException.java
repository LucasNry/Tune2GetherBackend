package com.t2g.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "There is already a user registered with this address")
public class UserAlreadyExistsException extends Exception {
}
