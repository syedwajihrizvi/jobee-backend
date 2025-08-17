package com.rizvi.jobee.exceptions;

import java.io.IOException;

public class AmazonS3Exception extends IOException {
    public AmazonS3Exception(String message) {
        super(message);
    }

    public AmazonS3Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public AmazonS3Exception(Throwable cause) {
        super(cause);
    }

}
