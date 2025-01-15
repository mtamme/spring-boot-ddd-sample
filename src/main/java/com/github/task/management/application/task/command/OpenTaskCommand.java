package com.github.task.management.application.task.command;

public record OpenTaskCommand(String projectId, String summary, String description) {
}
