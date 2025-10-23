/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withParameter
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import org.junit.Ignore
import org.junit.Test

class KonsistDiTest {
    @Test
    fun `class annotated with @Inject should not have constructors with @Assisted parameter`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withAnnotationOf(Inject::class)
            .assertTrue(
                additionalMessage = "Class with @Assisted parameter in constructor should be annotated with @AssistedInject and not @Inject"
            ) { classDeclaration ->
                classDeclaration.constructors
                    .withParameter { parameterDeclaration ->
                        parameterDeclaration.hasAnnotationOf(Assisted::class)
                    }
                    .isEmpty()
            }
    }

    @Ignore("Disabled to give time to branch and private module to remove the annotation")
    @Test
    fun `class annotated with @ContributesBinding does not need to be annotated with @Inject anymore`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withAnnotationOf(ContributesBinding::class)
            .assertFalse { classDeclaration ->
                classDeclaration.hasAnnotationOf(Inject::class)
            }
    }
}
