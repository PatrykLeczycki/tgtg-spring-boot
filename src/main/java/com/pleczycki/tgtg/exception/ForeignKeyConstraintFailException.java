package com.pleczycki.tgtg.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ForeignKeyConstraintFailException extends RuntimeException {
    public ForeignKeyConstraintFailException(String msg) {
        super(msg);
    }
}