package com.example.businessmanagement2.service.user;

import lombok.Getter;

@Getter
public class UserEntityNotFoundException extends RuntimeException {

  private final long userId;

  public UserEntityNotFoundException(long userId) {
    super("UserEntity (id = " + userId + ") is not found.");
    this.userId = userId;
  }
}
