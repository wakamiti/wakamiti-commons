/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.junit;


import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDirFactory;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;


public class TargetTempDirFactory implements TempDirFactory {

    private static final Path TARGET = Path.of("target").resolve("temp-test");

    @Override
    public Path createTempDirectory(
            AnnotatedElementContext elementContext,
            ExtensionContext extensionContext
    ) throws Exception {
        String methodName = extensionContext.getTestMethod()
                .map(Method::getName)
                .orElse("unknown-test");
        return Files.createDirectories(TARGET.resolve(methodName));
    }

}
