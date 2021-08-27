/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.migrate.guava

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaRecipeTest

class NoGuavaAtomicsNewReferenceTest : JavaRecipeTest {
    override val parser: JavaParser
        get() = JavaParser.fromJavaVersion().classpath("guava").build()

    override val recipe: Recipe
        get() = NoGuavaAtomicsNewReference()

    @Test
    fun noNewReference() = assertChanged(
        before = """
            import com.google.common.util.concurrent.Atomics;

            class Test {
                Object o1 = Atomics.newReference();
                Object o2 = Atomics.newReference(0);
            }
        """,
        after = """
            import java.util.concurrent.atomic.AtomicReference;

            class Test {
                Object o1 = new AtomicReference<>();
                Object o2 = new AtomicReference<>(0);
            }
        """
    )
}
