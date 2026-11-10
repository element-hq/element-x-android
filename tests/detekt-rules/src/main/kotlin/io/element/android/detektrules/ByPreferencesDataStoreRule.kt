/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.detektrules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import dev.detekt.api.RuleName
import org.jetbrains.kotlin.psi.KtPropertyDelegate

class ByPreferencesDataStoreRule(config: Config) : Rule(
    config = config,
    description = "Avoid using `by preferencesDataStore(...)`, use `PreferenceDataStoreFactory.create(name)`instead.",
) {
    override val ruleName = RuleName("ByPreferencesDataStoreNotAllowed")

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        if (delegate.containingKtFile.name == "DefaultPreferencesDataStoreFactory.kt") {
            // Skip the rule for the DefaultPreferencesDataStoreFactory implementation
            return
        }

        if (delegate.text.startsWith("by preferencesDataStore")) {
            report(
                Finding(
                    entity = Entity.from(delegate),
                    message = "Use `PreferenceDataStoreFactory.create(name)` instead of `by preferencesDataStore(...)`."
                )
            )
        }
    }
}
