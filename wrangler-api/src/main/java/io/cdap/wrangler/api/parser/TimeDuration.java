/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a time duration token like "500ms", "2.5s", etc.
 */
public class TimeDuration implements Token {
    private static final Pattern PATTERN = Pattern.compile("(?i)^([0-9]*\\.?[0-9]+)\\s*(ms|s|m|h)?$");
    private final long millis;

    public TimeDuration(String input) {
        super();
        this.millis = parseMillis(input);
    }

    private long parseMillis(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: " + input);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2) == null ? "ms" : matcher.group(2).toLowerCase();

        switch (unit) {
            case "ms":
                return (long) value;
            case "s":
                return (long) (value * 1000);
            case "m":
                return (long) (value * 60 * 1000);
            case "h":
                return (long) (value * 60 * 60 * 1000);
            default:
                throw new IllegalArgumentException("Unknown time duration unit: " + unit);
        }
    }

    public long getMillis() {
        return millis;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public TokenType type() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }
}
