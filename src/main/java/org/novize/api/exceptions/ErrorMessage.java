package org.novize.api.exceptions;




import lombok.Builder;

import java.util.Date;

/**
 * ErrorMessage is a record that represents an error message.
 * It contains the status code, timestamp, message, and description of the error.
 *
 * @param statusCode  the HTTP status code
 * @param timestamp   the timestamp of the error
 * @param message     the error message
 * @param description a detailed description of the error
 */
@Builder
public record ErrorMessage(int statusCode, Date timestamp, String message, String description) {

}
