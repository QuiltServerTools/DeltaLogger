package com.github.fabricservertools.deltalogger.gql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This example code chose to use GSON as its JSON parser. Any JSON parser should be fine
 */
public class JsonKit {
    static final Gson GSON = new GsonBuilder()
            //
            // This is important because the graphql spec says that null values should be present
            //
            .serializeNulls()
            .create();

    public static String toJson(Object result) {
        return GSON.toJson(result);
    }

    public static Map<String, Object> toMap(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().length() == 0) {
            return Collections.emptyMap();
        }
        // gson uses type tokens for generic input like Map<String,Object>
        TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {
        };
        Map<String, Object> map = GSON.fromJson(jsonStr, typeToken.getType());
        return map == null ? Collections.emptyMap() : map;
    }

    private static Map<String, Object> getVariables(Object variables) {
      if (variables instanceof Map) {
          Map<?, ?> inputVars = (Map) variables;
          Map<String, Object> vars = new HashMap<>();
          inputVars.forEach((k, v) -> vars.put(String.valueOf(k), v));
          return vars;
      }
      return JsonKit.toMap(String.valueOf(variables));
  }
}