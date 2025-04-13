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
 * Represents a byte size token like "10KB", "1.5MB", etc.
 */
public class ByteSize implements Token {
    private static final Pattern PATTERN = Pattern.compile("(?i)^([0-9]*\\.?[0-9]+)\\s*(b|kb|mb|gb|tb)?$");
    private final long bytes;

    public ByteSize(String input) {
        super();
        this.bytes = parseBytes(input);
    }

    private long parseBytes(String input) {
        Matcher matcher = PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: " + input);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2) == null ? "b" : matcher.group(2).toLowerCase();

        switch (unit) {
            case "b":
                return (long) value;
            case "kb":
                return (long) (value * 1024);
            case "mb":
                return (long) (value * 1024 * 1024);
            case "gb":
                return (long) (value * 1024 * 1024 * 1024);
            case "tb":
                return (long) (value * 1024L * 1024 * 1024 * 1024);
            default:
                throw new IllegalArgumentException("Unknown byte size unit: " + unit);
        }
    }

    public long getBytes() {
        return bytes;
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
