/*
 * Copyright © 2017-2019 Cask Data, Inc.
 * // Optional: Add your own copyright year/name
 * // Copyright © 2023 Lakshman
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
package io.cdap.directives.aggregates; // Corrected package name based on previous errors

// Group 1: Non-java/javax (Alphabetical Order)
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.etl.api.StageMetrics;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TokenGroup;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.annotations.PublicEvolving;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Properties; // Needed for initialize
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.Token;     // Needed for initialize
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.parser.MapArguments;

// Group 2: java/javax (Alphabetical Order)
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList; // Needed for initialize (reflection approach)
import java.util.Collections;
import java.util.List;
import java.util.Map;      // Needed for initialize
import java.util.Objects;

/**
 * Aggregate directive to calculate statistics (total/average) for byte size
 * and time duration columns across multiple rows.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates byte size (total bytes) and time duration (average nanos) columns.")
@PublicEvolving(deprecated = false)
public class AggregateStatsDirective implements Directive {
    // Argument Fields
    private String sizeSourceCol;
    private String timeSourceCol;
    private String sizeTargetCol;
    private String timeTargetCol;

    // State Keys
    private static final String TOTAL_BYTES_KEY = "aggstats.total.bytes";
    private static final String TOTAL_NANOS_KEY = "aggstats.total.nanos";
    private static final String ROW_COUNT_KEY = "aggstats.row.count";
    private static final String VALID_TIME_COUNT_KEY = "aggstats.valid.time.count";

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("size_source", TokenType.COLUMN_NAME);
        builder.define("time_source", TokenType.COLUMN_NAME);
        builder.define("size_target", TokenType.COLUMN_NAME);
        builder.define("time_target", TokenType.COLUMN_NAME);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        // Using named argument access ONLY
        if (!args.contains("size_source") || args.type("size_source") != TokenType.COLUMN_NAME) {
             throw new DirectiveParseException("Required argument 'size_source:' missing or not a column.");
        }
        this.sizeSourceCol = ((ColumnName) args.value("size_source")).value();

        if (!args.contains("time_source") || args.type("time_source") != TokenType.COLUMN_NAME) {
             throw new DirectiveParseException("Required argument 'time_source:' missing or not a column.");
        }
        this.timeSourceCol = ((ColumnName) args.value("time_source")).value();

        if (!args.contains("size_target") || args.type("size_target") != TokenType.COLUMN_NAME) {
             throw new DirectiveParseException("Required argument 'size_target:' missing or not a column.");
        }
        this.sizeTargetCol = ((ColumnName) args.value("size_target")).value();

        if (!args.contains("time_target") || args.type("time_target") != TokenType.COLUMN_NAME) {
             throw new DirectiveParseException("Required argument 'time_target:' missing or not a column.");
        }
        this.timeTargetCol = ((ColumnName) args.value("time_target")).value();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
         if (rows.isEmpty()) {
             return Collections.emptyList();
         }

        // Get state variables (using get(key) and casting)
        Object totalBytesObj = context.getTransientStore().get(TOTAL_BYTES_KEY);
        Object totalNanosObj = context.getTransientStore().get(TOTAL_NANOS_KEY);
        Object countObj = context.getTransientStore().get(ROW_COUNT_KEY);
        Object validTimeCountObj = context.getTransientStore().get(VALID_TIME_COUNT_KEY);

        long totalBytes = (totalBytesObj instanceof Long) ? (Long) totalBytesObj : 0L;
        long totalNanos = (totalNanosObj instanceof Long) ? (Long) totalNanosObj : 0L;
        long count = (countObj instanceof Long) ? (Long) countObj : 0L;
        long validTimeCount = (validTimeCountObj instanceof Long) ? (Long) validTimeCountObj : 0L;

         for (Row row : rows) {
             count++;
             Object sizeValue = row.getValue(this.sizeSourceCol);
             // <<< Apply Brace Formatting >>>
             if (sizeValue != null) {
                 try {
                     long currentBytes = parseByteSizeValue(sizeValue);
                     totalBytes += currentBytes;
                 } catch (Exception e) {
                      context.getMetrics().count("aggstats.size.parse.errors", 1);
                 }
             } else {
                  context.getMetrics().count("aggstats.size.null.skipped", 1);
             }

             Object timeValue = row.getValue(this.timeSourceCol);
             // <<< Apply Brace Formatting >>>
              if (timeValue != null) {
                 try {
                     long currentNanos = parseTimeDurationValue(timeValue);
                     totalNanos += currentNanos;
                     validTimeCount++;
                 } catch (Exception e) {
                      context.getMetrics().count("aggstats.time.parse.errors", 1);
                 }
             } else {
                  context.getMetrics().count("aggstats.time.null.skipped", 1);
             }
         }
         // ... set state variables ...
         context.getTransientStore().set(TransientVariableScope.GLOBAL, TOTAL_BYTES_KEY, totalBytes);
         context.getTransientStore().set(TransientVariableScope.GLOBAL, TOTAL_NANOS_KEY, totalNanos);
         context.getTransientStore().set(TransientVariableScope.GLOBAL, ROW_COUNT_KEY, count);
         context.getTransientStore().set(TransientVariableScope.GLOBAL, VALID_TIME_COUNT_KEY, validTimeCount);

         return Collections.emptyList();
    }

    // Helper method to parse byte size
    private long parseByteSizeValue(Object value) throws IllegalArgumentException {
        // <<< Apply Brace Formatting >>>
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof ByteSize) {
             return ((ByteSize) value).getBytes();
        } else if (value instanceof String) {
            return new ByteSize((String) value).getBytes();
        } else if (value instanceof Number) {
             return ((Number) value).longValue();
        }
        // Break long line if needed
        throw new IllegalArgumentException(
            "Cannot parse value '" + value + "' as byte size.");
    }

    // Helper method to parse time duration
     private long parseTimeDurationValue(Object value) throws IllegalArgumentException {
         // <<< Apply Brace Formatting >>>
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof TimeDuration) {
             return ((TimeDuration) value).getNanoseconds();
        } else if (value instanceof String) {
            return new TimeDuration((String) value).getNanoseconds();
        } else if (value instanceof Number) {
             return ((Number) value).longValue();
        }
         // Break long line if needed
        throw new IllegalArgumentException(
            "Cannot parse value '" + value + "' as time duration.");
    }


    // Final result generation
    public List<Row> getFinalResult(ExecutorContext context) {
        // Get final values (using get(key) and casting)
        Object finalTotalBytesObj = context.getTransientStore().get(TOTAL_BYTES_KEY);
        Object finalTotalNanosObj = context.getTransientStore().get(TOTAL_NANOS_KEY);
        Object finalCountObj = context.getTransientStore().get(ROW_COUNT_KEY);
        Object finalValidTimeCountObj = context.getTransientStore().get(VALID_TIME_COUNT_KEY);
        long finalTotalBytes = (finalTotalBytesObj instanceof Long) ? (Long) finalTotalBytesObj : 0L;
        long finalTotalNanos = (finalTotalNanosObj instanceof Long) ? (Long) finalTotalNanosObj : 0L;
        long currentFinalCount = (finalCountObj instanceof Long) ? (Long) finalCountObj : 0L;
        long currentValidTimeCount = (finalValidTimeCountObj instanceof Long) ? (Long) finalValidTimeCountObj : 0L;


        Row resultRow = new Row();
        // <<< Apply Brace Formatting >>>
         if (currentFinalCount == 0) {
             resultRow.add(this.sizeTargetCol, 0L);
             resultRow.add(this.timeTargetCol, 0.0);
             resultRow.add("aggregate_count", 0L);
         } else {
             long finalSizeValue = finalTotalBytes;
             double finalTimeValue = (currentValidTimeCount == 0) ? 0.0 :
                                     (double) finalTotalNanos / currentValidTimeCount;
             resultRow.add(this.sizeTargetCol, finalSizeValue);
             resultRow.add(this.timeTargetCol, finalTimeValue);
             resultRow.add("aggregate_count", currentFinalCount);
         }
        context.getTransientStore().reset(TransientVariableScope.GLOBAL);
        return Collections.singletonList(resultRow);
    }

    @Override
    public void destroy() {
        // No-op
    }
}
