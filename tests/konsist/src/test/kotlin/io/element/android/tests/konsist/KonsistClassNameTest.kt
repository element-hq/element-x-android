/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.bumble.appyx.core.node.Node
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllParentsOf
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withNameContaining
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.ext.list.withoutNameStartingWith
import com.lemonappdev.konsist.api.verify.assertEmpty
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

    @Test
    fun `Fake classes must be named using Fake and the interface it fakes`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameContaining("Fake")
            .withoutName(
                "FakeFileSystem",
                "FakeImageLoader",
                "FakeRustRoom",
            )
            .assertTrue {
                val interfaceName = it.name.replace("Fake", "")
                it.name.startsWith("Fake") &&
                    it.parents().any { parent -> parent.name.replace(".", "") == interfaceName }
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
