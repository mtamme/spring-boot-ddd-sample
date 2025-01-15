package com.github.task.management.application.task.command;

public record UnassignTaskCommand(String taskId, String assigneeName) {
}
