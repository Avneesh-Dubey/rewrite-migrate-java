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
package org.openrewrite.java.migrate.javaee;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.migrate.javax.HttpSessionInvalidate;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class HttpSessionInvalidateTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "javax.servlet-3.0"))
          .recipe(new HttpSessionInvalidate());
    }

    @DocumentExample
    @Test
    void useLogoutWhenHttpServletRequestExistsInScope() {
        rewriteRun(
          //language=java
          java(
            """
              import javax.servlet.http.HttpServletResponse;
              import javax.servlet.http.HttpServletRequest;
              import javax.servlet.http.HttpSession;

              class Foo {
                  void logOut(HttpServletRequest req, HttpServletResponse res) {
                      HttpSession session = req.getSession(false);
                      session.invalidate();
                      res.sendRedirect("login.html");
                  }
              }
              """,
            """
              import javax.servlet.http.HttpServletResponse;
              import javax.servlet.http.HttpServletRequest;
              import javax.servlet.http.HttpSession;

              class Foo {
                  void logOut(HttpServletRequest req, HttpServletResponse res) {
                      HttpSession session = req.getSession(false);
                      req.logout();
                      res.sendRedirect("login.html");
                  }
              }
              """
          )
        );
    }

    @Test
    void noChangeNeeded() {
        rewriteRun(
          //language=java
          java(
            """
              import javax.servlet.http.HttpServletResponse;
              import javax.servlet.http.HttpServletRequest;
              import javax.servlet.http.HttpSession;

              class Foo {
                  void logOut(HttpServletRequest req, HttpServletResponse res) {
                      HttpSession session = req.getSession();
                      req.logout();
                      res.sendRedirect("login.html");
                  }
              }
              """
          )
        );
    }

    @Test
    void noChangeCannotFindServletRequest() {
        rewriteRun(
          //language=java
          java(
            """
              import javax.servlet.http.HttpServletResponse;
              import javax.servlet.http.HttpSession;

              class Foo {
                  void logOut(HttpSession session, HttpServletResponse res) {
                      session.invalidate();
                      res.sendRedirect("login.html");
                  }
              }
              """
          )
        );
    }
}
