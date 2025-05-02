
package org.novize.api.exceptions;

/**
 * Exception die geworfen wird, wenn ein Benutzer nicht gefunden werden kann.
 * Erweitert RuntimeException, um die standardmäßige Spring-Ausnahmebehandlung zu unterstützen.
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Erstellt eine neue UserNotFoundException mit einer Nachricht.
     *
     * @param message die Fehlermeldung
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Erstellt eine neue UserNotFoundException mit einer Nachricht und einer Ursache.
     *
     * @param message die Fehlermeldung
     * @param cause die Ursache des Fehlers
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Erstellt eine neue UserNotFoundException mit einer Ursache.
     *
     * @param cause die Ursache des Fehlers
     */
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}