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

package io.cdap.directives;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import org.stringtemplate.v4.misc.Aggregate;

import java.util.List;

/**
 * A directive to aggregate total size and time across rows.
 */
public class AggregateStatsDirective extends Aggregate implements Directive {

    private String sizeCol;
    private String timeCol;
    private String outputSizeCol;
    private String outputTimeCol;

    private static final String SIZE_KEY = "total_bytes";
    private static final String TIME_KEY = "total_millis";


    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("sizeCol", TokenType.COLUMN_NAME);
        builder.define("timeCol", TokenType.COLUMN_NAME);
        builder.define("outputSizeCol", TokenType.COLUMN_NAME);
        builder.define("outputTimeCol", TokenType.COLUMN_NAME);

        UsageDefinition def = builder.build();
        return def;
    }

    @Override
    public void initialize(Arguments arguments) {
        this.sizeCol = arguments.value("sizeCol").toString();
        this.timeCol = arguments.value("timeCol").toString();
        this.outputSizeCol = arguments.value("outputSizeCol").toString();
        this.outputTimeCol = arguments.value("outputTimeCol").toString();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        long totalBytes = 0;
        long totalMillis = 0;

        for (Row row : rows) {
            Object sizeObj = row.getValue(sizeCol);
            Object timeObj = row.getValue(timeCol);

            long size = parseBytes(sizeObj);
            long time = parseMillis(timeObj);

            totalBytes += size;
            totalMillis += time;
        }

        // Store results in context for finalization
        context.getProperties().put(SIZE_KEY, String.valueOf(totalBytes));
        context.getProperties().put(TIME_KEY, String.valueOf(totalMillis));

        return rows; // aggregation step returns original rows
    }

    @Override
    public void destroy() {

    }

    public List<Row> finalize(ExecutorContext context) throws DirectiveExecutionException {
        long totalBytes = (long) context.getTransientStore().get(SIZE_KEY);
        long totalMillis = (long) context.getTransientStore().get(TIME_KEY);

        double totalMB = totalBytes / (1024.0 * 1024.0);
        double totalSeconds = totalMillis / 1000.0;

        Row result = new Row();
        result.add(outputSizeCol, totalMB);
        result.add(outputTimeCol, totalSeconds);

        List<Row> rows = new java.util.ArrayList<>();
        rows.add(result);
        return rows; // one row with totals
    }

    private long parseBytes(Object input) {
        if (input instanceof String) {
            return new ByteSize((String) input).getBytes();
        } else if (input instanceof Number) {
            return ((Number) input).longValue();
        } else {
            throw new IllegalArgumentException("Invalid byte size input: " + input);
        }
    }

    private long parseMillis(Object input) {
        if (input instanceof String) {
            return new TimeDuration((String) input).getMillis();
        } else if (input instanceof Number) {
            return ((Number) input).longValue();
        } else {
            throw new IllegalArgumentException("Invalid time duration input: " + input);
        }
    }
}
