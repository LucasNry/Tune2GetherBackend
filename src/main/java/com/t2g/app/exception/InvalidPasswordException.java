package com.t2g.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "The password provided is incorrect")
public class InvalidPasswordException extends Exception {
}
