package io.github.abdullahcxd.soliditycore.exception;

public class SolidityException extends RuntimeException {
    public SolidityException(String message) {
        super(message);
    }
    public SolidityException(Throwable throwable) { super(throwable); }
    public SolidityException(String message, Throwable throwable) { super(message, throwable); }
}
