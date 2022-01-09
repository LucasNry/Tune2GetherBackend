package com.t2g.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "The provided link is from an unsupported streaming service")
public class StreamingServiceNotSupported extends Exception {
}
