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

import com.google.gson.JsonElement;
import io.cdap.cdap.etl.api.Lookup;
import io.cdap.cdap.etl.api.StageMetrics;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.parser.TokenType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregateStatsDirectiveTest {

    private AggregateStatsDirective directive;
    private List<Row> inputRows;
    private ExecutorContext context;

    @Before
    public void setup() {
        directive = new AggregateStatsDirective();

        directive.initialize(new Arguments() {
            private final Map<String, Object> args;

            {
                args = new HashMap<>();
                args.put("sizeCol", "bytes");
                args.put("timeCol", "duration");
                args.put("outputSizeCol", "total_size_mb");
                args.put("outputTimeCol", "total_duration_sec");
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean contains(String name) {
                return false;
            }

            @Override
            public TokenType type(String name) {
                return null;
            }

            @Override
            public int line() {
                return 0;
            }

            @Override
            public int column() {
                return 0;
            }

            @Override
            public String source() {
                return "";
            }

            @Override
            public JsonElement toJson() {
                return null;
            }

            @Override
            public Token value(String name) {
                Object val = args.get(name);
                return new io.cdap.wrangler.api.parser.Text(val.toString());
            }


            public boolean has(String name) {
                return args.containsKey(name);
            }
            public Collection<String> getNames() {
                return args.keySet();
            }
        });

        inputRows = new ArrayList<>();
        inputRows.add(new Row("bytes", "1MB").add("duration", "2s"));
        inputRows.add(new Row("bytes", "512KB").add("duration", "500ms"));

        context = new ExecutorContext() {
            private final Map<String, Object> store = new HashMap<>();

            @Override
            public <T> Lookup<T> provide(String s, Map<String, String> map) {
                return null;
            }

            @Override
            public Environment getEnvironment() {
                return null;
            }

            @Override
            public String getNamespace() {
                return "";
            }

            @Override
            public StageMetrics getMetrics() {
                return null;
            }

            @Override
            public String getContextName() {
                return "";
            }

            @Override public Map<String, String> getProperties() {
                return new HashMap<>();
            }

            @Override
            public URL getService(String applicationId, String serviceId) {
                return null;
            }

            @Override public TransientStore getTransientStore() {
                return (TransientStore) store;
            }
        };
    }

    @Test
    public void testExecuteAndFinalize() throws DirectiveExecutionException {
        directive.execute(inputRows, context);

        // Simulate finalize step
        List<Row> result = directive.finalize(context);

        Row row = result.get(0);
        double totalSizeMB = (1 * 1024 * 1024 + 512 * 1024) / (1024.0 * 1024.0);
        double totalTimeSec = (2000 + 500) / 1000.0;

        Assert.assertEquals(totalSizeMB, row.getValue("total_size_mb"));
        Assert.assertEquals(totalTimeSec, row.getValue("total_duration_sec"));
    }
}
