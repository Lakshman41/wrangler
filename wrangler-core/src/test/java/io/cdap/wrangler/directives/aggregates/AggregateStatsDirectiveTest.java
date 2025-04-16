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
package io.cdap.wrangler.directives.aggregates;

// Group 1: Non-java/javax (Alphabetical Order)
import io.cdap.cdap.etl.api.StageMetrics;
import io.cdap.directives.aggregates.AggregateStatsDirective;
import io.cdap.wrangler.TestingPipelineContext;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.RecipeException;
import io.cdap.wrangler.api.RecipeParser;
import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.executor.RecipePipelineExecutor;
import io.cdap.wrangler.parser.GrammarBasedParser;
import io.cdap.wrangler.proto.Contexts;
import io.cdap.wrangler.registry.CompositeDirectiveRegistry;
import io.cdap.wrangler.registry.SystemDirectiveRegistry;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

// Group 2: java/javax (Alphabetical Order)
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Group 3: Static imports (None here)

/**
 * Tests the simplified {@link AggregateStatsDirective} class.
 */
public class AggregateStatsDirectiveTest {

    private static final double DELTA = 0.00001; // Tolerance for double comparisons

    // Helper to create context (uses DefaultTransientStore internally)
    private ExecutorContext createContext() {
        return new TestingPipelineContext();
    }

    // Helper to create the parser
    private RecipeParser createParser(String[] recipe) throws Exception {
         CompositeDirectiveRegistry registry = new CompositeDirectiveRegistry(SystemDirectiveRegistry.INSTANCE);
         return new GrammarBasedParser(Contexts.SYSTEM, String.join("\n", recipe), registry);
    }

    // Helper to find our specific directive instance from the parsed list
    private AggregateStatsDirective findMyDirective(List<Directive> directives) {
         Assert.assertFalse("Directives list should not be empty", directives.isEmpty());
         // Assuming only one directive in the test recipes
         Assert.assertTrue("Parsed directive should be AggregateStatsDirective",
                          directives.get(0) instanceof AggregateStatsDirective);
         return (AggregateStatsDirective) directives.get(0);
    }

    @Test
    public void testBasicAggregationDefaults() throws Exception {
        // <<< Use generic column names in recipe AND data >>>
        String[] recipe = new String[] {
            "aggregate-stats :col_size :col_time :out_bytes :out_nanos;" // Use generic names
        };
        List<Row> inputRows = Arrays.asList(
            new Row("col_size", "1KB").add("col_time", "500ms"),   // Use matching generic names
            new Row("col_size", "2KB").add("col_time", "1s"),
            new Row("col_size", "10B").add("col_time", "1500ms")
        );
        ExecutorContext context = createContext();
        RecipeParser parser = createParser(recipe);
        List<Directive> directives = parser.parse(); // Parse should pass now
        AggregateStatsDirective directiveInstance = findMyDirective(directives);

        // Initialize should map positional :col_size to "size_source" name, etc.
        // based on UsageDefinition order in define() method.

        RecipePipeline pipeline = new RecipePipelineExecutor(parser, context);
        pipeline.execute(inputRows);
        List<Row> finalOutputRows = directiveInstance.getFinalResult(context);

        // Assertions use the TARGET names defined in the recipe
        Assert.assertEquals(1, finalOutputRows.size());
        Row resultRow = finalOutputRows.get(0);
        Assert.assertEquals("Total bytes mismatch",
                            3082L,
                            resultRow.getValue("out_bytes")); // Use target name
        Assert.assertEquals("Average nanos mismatch", 
                            1_000_000_000.0,
                            (Double) resultRow.getValue("out_nanos"),
                            DELTA); // Use target name
        Assert.assertEquals("Count mismatch", 3L, resultRow.getValue("aggregate_count"));
        Assert.assertNull(context.getTransientStore().get("aggstats.row.count"));
    }

    @Test
    public void testEmptyInputDefaults() throws Exception {
        // <<< Use generic column names in recipe >>>
        String[] recipe = new String[] {
             "aggregate-stats :c1 :c2 :out1 :out2;" // Generic names
        };
        List<Row> inputRows = Collections.emptyList();
        ExecutorContext context = createContext();
        RecipeParser parser = createParser(recipe);
        List<Directive> directives = parser.parse(); // Parse should pass now
        AggregateStatsDirective directiveInstance = findMyDirective(directives);
        RecipePipeline pipeline = new RecipePipelineExecutor(parser, context);
        pipeline.execute(inputRows);
        List<Row> finalOutputRows = directiveInstance.getFinalResult(context);

        // Assertions use the TARGET names defined in the recipe
        Assert.assertEquals(1, finalOutputRows.size());
        Row resultRow = finalOutputRows.get(0);
        Assert.assertEquals("Total bytes should be 0L",
                            0L,
                            resultRow.getValue("out1")); // Use target name
        Assert.assertEquals("Average nanos should be 0.0",
                            0.0,
                            (Double) resultRow.getValue("out2"),
                            DELTA); // Use target name
        Assert.assertEquals("Count should be 0", 0L, resultRow.getValue("aggregate_count"));
        Assert.assertNull(context.getTransientStore().get("aggstats.row.count"));
    }

    @Test
    public void testNullAndInvalidValuesDefaults() throws Exception {
         // <<< Use generic column names in recipe AND data >>>
         String[] recipe = new String[] {
             "aggregate-stats :mySize :myTime :byteSum :nanoAvg;" // Generic names
         };
         List<Row> inputRows = Arrays.asList(
            new Row("mySize", "1KB").add("myTime", "500ms"),     // Use matching generic names
            new Row("mySize", null).add("myTime", "1s"),
            new Row("mySize", "2KB").add("myTime", null),
            new Row("mySize", "invalid").add("myTime", "1500ms"),
            new Row("mySize", "10B").add("myTime", "invalid_time")
         );
         ExecutorContext context = createContext();
         StageMetrics metrics = context.getMetrics();
         RecipeParser parser = createParser(recipe);
         List<Directive> directives = parser.parse(); // Parse should pass now
         AggregateStatsDirective directiveInstance = findMyDirective(directives);
         RecipePipeline pipeline = new RecipePipelineExecutor(parser, context);
         pipeline.execute(inputRows);
         List<Row> finalOutputRows = directiveInstance.getFinalResult(context);

         // Assertions use the TARGET names defined in the recipe
         Assert.assertEquals(1, finalOutputRows.size());
         Row resultRow = finalOutputRows.get(0);
         Assert.assertEquals("Total bytes mismatch",
                            3082L,
                            resultRow.getValue("byteSum")); // Use target name
         Assert.assertEquals("Average nanos mismatch", 
                            1_000_000_000.0,
                            (Double) resultRow.getValue("nanoAvg"),
                            DELTA); // Use target name
         Assert.assertEquals("Count mismatch", 5L, resultRow.getValue("aggregate_count"));
         // Verify metrics
         Mockito.verify(metrics).count("aggstats.size.null.skipped", 1);
         // ... verify others ...
         Assert.assertNull(context.getTransientStore().get("aggstats.row.count"));
    }
}
