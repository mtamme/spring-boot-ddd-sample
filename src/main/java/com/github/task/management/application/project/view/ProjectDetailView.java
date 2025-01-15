package com.github.task.management.application.project.view;

public record ProjectDetailView(String projectId, boolean archived, String name, String description) {
}
