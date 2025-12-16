package jaeger.de.miel.TodoAPI.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    ARCHIVED, DONE, IN_PROGRESS, TODO;

    @JsonCreator
    public static TaskStatus fromJson(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase()) {
            case "archived" -> ARCHIVED;
            case "done" -> DONE;
            case "in_progress" -> IN_PROGRESS;
            case "todo" -> TODO;
            default -> throw new IllegalArgumentException("Invalid TaskStatus: " + value);
        };
    }

    @JsonValue
    public String toJson() {
        return switch (this) {
            case ARCHIVED -> "archived";
            case DONE -> "done";
            case IN_PROGRESS -> "in_progress";
            case TODO -> "todo";
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case ARCHIVED -> "archived";
            case DONE -> "done";
            case IN_PROGRESS -> "in_progress";
            case TODO -> "todo";
        };
    }

}