/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.anvilcodegen

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ContributesNodeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val enableLogging = environment.options["enableLogging"]?.toBoolean() ?: false
        return ContributesNodeProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
            config = ContributesNodeProcessor.Config(enableLogging = enableLogging),
        )
    }
}
