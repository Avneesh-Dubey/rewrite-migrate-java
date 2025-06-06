/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.migrate.util;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesJavaVersion;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Unary.Type;
import org.openrewrite.java.tree.Statement;

import java.time.Duration;

public class OptionalNotEmptyToIsPresent extends Recipe {
    @Override
    public String getDisplayName() {
        return "Prefer `Optional.isPresent()`";
    }

    @Override
    public String getDescription() {
        return "Prefer `Optional.isPresent()` instead of using `!Optional.isEmpty()` in Java 11 or higher.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        MethodMatcher optionalIsPresentMatcher = new MethodMatcher("java.util.Optional isEmpty()");
        TreeVisitor<?, ExecutionContext> check = Preconditions.and(
                new UsesJavaVersion<>(11),
                new UsesMethod<>(optionalIsPresentMatcher));
        return Preconditions.check(check, new JavaVisitor<ExecutionContext>() {
            @Override
            public J visitStatement(Statement s, ExecutionContext ctx) {
                if (s instanceof J.Unary) {
                    J.Unary unary = (J.Unary) s;
                    if (unary.getOperator() == Type.Not && optionalIsPresentMatcher.matches(unary.getExpression())) {
                        return JavaTemplate.apply("#{any(java.util.Optional)}.isPresent()",
                                getCursor(),
                                unary.getCoordinates().replace(),
                                ((J.MethodInvocation) unary.getExpression()).getSelect());
                    }
                }
                return super.visitStatement(s, ctx);
            }
        });
    }
}
