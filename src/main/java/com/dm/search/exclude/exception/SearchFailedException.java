package com.dm.search.exclude.exception;


public class SearchFailedException extends RuntimeException {
    public SearchFailedException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}