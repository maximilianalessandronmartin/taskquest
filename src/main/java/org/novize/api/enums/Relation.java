package org.novize.api.enums;

/**
 * Represents the relationship between a user and a task.
 * SHARED - Task is shared with the user by another user
 * OWNED - Task is created and owned by the user
 * ASSIGNED - Task is assigned to the user
 */
public enum Relation {
    SHARED,
    OWNED,
    ASSIGNED
}
