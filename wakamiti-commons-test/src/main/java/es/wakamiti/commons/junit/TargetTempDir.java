/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.commons.junit;

import org.junit.jupiter.api.io.CleanupMode;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.junit.jupiter.api.io.TempDir(
        factory = TargetTempDirFactory.class,
        cleanup = CleanupMode.NEVER
)
public @interface TargetTempDir {
}
