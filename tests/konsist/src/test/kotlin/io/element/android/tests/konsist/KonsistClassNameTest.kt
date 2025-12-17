/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bumble.appyx.core.node.Node
import com.google.common.truth.Truth.assertThat
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllParentsOf
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withNameContaining
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.ext.list.withoutNameStartingWith
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import io.element.android.libraries.architecture.BaseFlowNode
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
    fun `Classes extending 'BaseFlowNode' should have 'FlowNode' suffix`() {
        Konsist.scopeFromProject()
            .classes()
            .withAllParentsOf(BaseFlowNode::class)
            .assertTrue {
                it.name.endsWith("FlowNode")
            }
    }

    @Test
    fun `Classes extending 'PreviewParameterProvider' name MUST end with 'Provider' and MUST contain provided class name`() {
        Konsist.scopeFromProduction()
            .classes()
            .withAllParentsOf(PreviewParameterProvider::class)
            .withoutName(
                "AspectRatioProvider",
                "EditableAvatarViewUriProvider",
                "LoginModeViewErrorProvider",
                "OverlapRatioProvider",
                "TextFileContentProvider",
            )
            .also {
                // Check that classes are actually found
                assertThat(it.size).isGreaterThan(100)
            }
            .assertTrue { klass ->
                // Cannot find a better way to get the type of the generic
                val providedType = klass.text
                    .substringAfter("PreviewParameterProvider<")
                    .substringBefore(">")
                    // Get the substring before the first '<' to remove the generic type
                    .substringBefore("<")
                    .removeSuffix("?")
                    .replace(".", "")
                val name = klass.name
                name.endsWith("Provider") &&
                    name.endsWith("PreviewProvider").not() &&
                    name.contains(providedType)
            }
    }

    @Test
    fun `Fake classes must be named using Fake and the interface it fakes`() {
        var failingCases = 0
        val failingCasesList = listOf(
            "FakeWrongClassName",
            "FakeWrongClassSubInterfaceName",
        )
        Konsist.scopeFromProject()
            .classes()
            .withNameContaining("Fake")
            .withoutName(
                "FakeFileSystem",
                "FakeImageLoader",
                "FakeListenableFuture",
            )
            .assertTrue {
                val interfaceName = it.name
                    .replace("FakeFfi", "")
                    .replace("Fake", "")
                val result = it.name.startsWith("Fake") &&
                    it.parents().any { parent ->
                        val parentName = parent.name.replace(".", "")
                        parentName == interfaceName
                    }
                if (!result && it.name in failingCasesList) {
                    failingCases++
                    true
                } else {
                    result
                }
            }
        assertThat(failingCases).isEqualTo(failingCasesList.size)
    }

    @Test
    fun `All Classes that override a class from the Ffi layer must have 'FakeFfi' prefix`() {
        Konsist.scopeFromTest()
            .classes()
            .withPackage("io.element.android.libraries.matrix.impl.fixtures.fakes")
            .assertTrue { klass ->
                val parentName = klass.parents().firstOrNull()?.name.orEmpty()
                klass.name == "FakeFfi$parentName"
            }
    }

    @Test
    fun `Class implementing interface should have name not end with 'Impl' but start with 'Default'`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("Impl")
            .withoutName("MediaUploadHandlerImpl")
            .assertEmpty(additionalMessage = "Class implementing interface should have name not end with 'Impl' but start with 'Default'")
    }

    @Test
    fun `Class with 'ContributeBinding' annotation should have allowed prefix`() {
        Konsist.scopeFromProject()
            .classes()
            .withAnnotationNamed("ContributesBinding")
            .withoutName(
                "Factory",
                "TimelineController",
                "TimelineMediaGalleryDataSource",
                "MetroWorkerFactory",
            )
            .withoutNameStartingWith(
                "Accompanist",
                "AES",
                "Android",
                "Asset",
                "Database",
                "DBov",
                "Default",
                "DataStore",
                "Enterprise",
                "Fdroid",
                "FileExtensionExtractor",
                "Internal",
                "LiveMediaTimeline",
                "KeyStore",
                "Matrix",
                "Noop",
                "Oss",
                "Preferences",
                "Rust",
                "SharedPreferences",
            )
            .assertEmpty()
    }
}
