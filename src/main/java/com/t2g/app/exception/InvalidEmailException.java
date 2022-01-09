package com.t2g.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "The email provided is incorrect or is not linked to an account")
public class InvalidEmailException extends Exception {
}
