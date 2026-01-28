package io.github.abdullahcxd.soliditycore.storage;

import io.github.abdullahcxd.soliditycore.exception.SolidityException;

/**
 * Exception thrown when storage operations fail.
 * Extends SolidityException to maintain consistency with the framework's exception hierarchy.
 */
public class StorageException extends SolidityException {

    /**
     * Creates a new StorageException with a message.
     *
     * @param message The error message
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Creates a new StorageException with a message and cause.
     *
     * @param message The error message
     * @param cause   The underlying cause
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new StorageException with a cause.
     *
     * @param cause The underlying cause
     */
    public StorageException(Throwable cause) {
        super(cause);
    }
}