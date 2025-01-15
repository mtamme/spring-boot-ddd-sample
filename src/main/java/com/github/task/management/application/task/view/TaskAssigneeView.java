package com.github.task.management.application.task.view;

public record TaskAssigneeView(String name) {

  public static TaskAssigneeView of(final String name) {
    if (name == null) {
      return null;
    }

    return new TaskAssigneeView(name);
  }
}
