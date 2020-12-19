package com.github.fabricservertools.deltalogger.gql;

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

  private static String joinErrStrings(Seq<String> xs) {
    return xs
      .intersperse("; ")
      .foldLeft(new StringBuilder(), StringBuilder::append)
      .toString();
  }

  public Validation<String, Tuple2<Integer,Integer>> validatePagination(int offset, int limit, int maxLimit) {
      return Validation
        .combine(validateOffset(offset), validateLimit(limit, maxLimit))
        .ap(Tuple2::new)
        .mapError(Validators::joinErrStrings);
  }

  private Validation<String, Integer> validateLimit(int limit, int maxLimit) {
    return limit < 0
      ? Validation.invalid("Limit cannot be less than 0")
      : limit >= maxLimit
      ? Validation.invalid("Limit cannot the greater than " + maxLimit)
      : Validation.valid(limit);
  }

  private Validation<String, Integer> validateOffset(int offset) {
    return offset < 0
      ? Validation.invalid("Offset cannot be less than 0")
      : Validation.valid(offset);
  }
}
