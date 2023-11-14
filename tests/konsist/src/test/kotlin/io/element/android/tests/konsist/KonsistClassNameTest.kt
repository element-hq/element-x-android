/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bumble.appyx.core.node.Node
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllParentsOf
import com.lemonappdev.konsist.api.verify.assertTrue
import io.element.android.libraries.architecture.Presenter
import org.junit.Test

class KonsistClassNameTest {
    @Test
    fun `Classes extending 'Presenter' should have 'Presenter' suffix`() {
        Konsist.scopeFromProject()
            .classes()
            .withAllParentsOf(Presenter::class)
            .assertTrue {
                it.name.endsWith("Presenter")
            }
    }

    @Test
    fun `Classes extending 'Node' should have 'Node' suffix`() {
        Konsist.scopeFromProject()
            .classes()
            .withAllParentsOf(Node::class)
            .assertTrue {
                it.name.endsWith("Node")
            }
    }

    @Test
    fun `Classes extending 'PreviewParameterProvider' name MUST end with 'Provider' and MUST contain provided class name`() {
        Konsist.scopeFromProject()
            .classes()
            .withAllParentsOf(PreviewParameterProvider::class)
            .assertTrue {
                // Cannot find a better way to get the type of the generic
                val providedType = it.text
                    .substringBefore(">")
                    .substringAfter("<")
                    // Get the substring before the first '<' to remove the generic type
                    .substringBefore("<")
                    .removeSuffix("?")
                    .replace(".", "")
                it.name.endsWith("Provider") && (it.name.contains("IconList") || it.name.contains(providedType))
            }
    }
}
