package com.example.lab5_practica3.common;

public final class OperationResult {
  public final boolean success;
  public final String message;
  private boolean consumed;

  public OperationResult(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public boolean consume() {
    if (consumed) return false;
    consumed = true;
    return true;
  }
}
