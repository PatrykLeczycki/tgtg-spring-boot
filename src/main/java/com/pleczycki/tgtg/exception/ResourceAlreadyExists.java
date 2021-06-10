package com.pleczycki.tgtg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExists extends RuntimeException {
    public ResourceAlreadyExists(String msg) {
        super(msg);
    }
}
