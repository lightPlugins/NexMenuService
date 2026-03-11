package io.nexstudios.menuservice.common.api.validation;

/**
 * Base exception type for menu API validation errors.
 */
public class MenuException extends RuntimeException {

  public MenuException(String message) {
    super(message);
  }

  public MenuException(String message, Throwable cause) {
    super(message, cause);
  }
}