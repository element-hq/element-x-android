/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.codegen

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ContributesNodeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val enableLogging = environment.options["enableLogging"]?.toBoolean() == true
        return ContributesNodeProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
            config = ContributesNodeProcessor.Config(enableLogging = enableLogging),
        )
    }
}
