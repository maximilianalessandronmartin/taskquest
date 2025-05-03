package org.novize.api.exceptions;

import java.io.Serial;
import java.io.Serializable;

public class FriendshipNotFoundException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public FriendshipNotFoundException(String message) {
        super(message);
    }

    public FriendshipNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FriendshipNotFoundException(Throwable cause) {
        super(cause);
    }
}
