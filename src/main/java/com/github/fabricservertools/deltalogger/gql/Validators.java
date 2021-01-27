package com.github.fabricservertools.deltalogger.gql;

import java.util.regex.Pattern;

import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

public class Validators {
  public static class PaginationInput {
    public final int offset;
    public final int limit;
    public PaginationInput(int offset, int limit) {
      this.offset = offset;
      this.limit = limit;
    }     
  }

  public static String joinErrStrings(Seq<String> xs) {
    return xs
      .intersperse("; ")
      .foldLeft(new StringBuilder(), StringBuilder::append)
      .toString();
  }

  public static Validation<String, Tuple2<Integer,Integer>> validatePagination(int offset, int limit, int maxLimit) {
      return Validation
        .combine(validateOffset(offset), validateLimit(limit, maxLimit))
        .ap(Tuple2::new)
        .mapError(Validators::joinErrStrings);
  }

  public static Validation<String, Integer> validateLimit(int limit, int maxLimit) {
    return limit < 0
      ? Validation.invalid("Limit cannot be less than 0")
      : limit > maxLimit
      ? Validation.invalid("Limit cannot the greater than " + maxLimit)
      : Validation.valid(limit);
  }

  public static Validation<String, Integer> validateOffset(int offset) {
    return offset < 0
      ? Validation.invalid("Offset cannot be less than 0")
      : Validation.valid(offset);
  }

  public static Validation<String, String> validateLength(String title, String input, int length) {
    if (input == null) return Validation.invalid(title + " cannot be empty.");
    if (input.length() < length) return Validation.invalid(title + " must be longer than " + length + " characters.");
    return Validation.valid(input);
  }

  public static Validation<String, String> validatePasswordSet(String password) {
    if (password == null) return Validation.invalid("Cannot have empty password.");

    if (!Pattern.matches("[\\da-zA-Z!@#$%\\?]+", password)) {
      return Validation.invalid(
        "Password must only contain letters a-z, A-Z, numbers, or special characters."
      );
    }
    return Validation.valid(password);
  }

  public static Validation<Seq<String>, String> validatePassword(String password) {
    return Validation.combine(
      validatePasswordSet(password),
      validateLength("Password", password, 10)
    ).ap((s1, s2) -> s1);
  }
}
