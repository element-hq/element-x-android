/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.detektrules

import io.github.detekt.psi.fileName
import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtPropertyDelegate

class ByPreferencesDataStoreRule(config: Config) : Rule(config) {
    override val issue: Issue = Issue(
        id = "ByPreferencesDataStoreNotAllowed",
        severity = Severity.Style,
        description = "Avoid using `by preferencesDataStore(...)`, use `PreferenceDataStoreFactory.create(name)`instead.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitPropertyDelegate(delegate: KtPropertyDelegate) {
        super.visitPropertyDelegate(delegate)

        if (delegate.containingKtFile.fileName == "DefaultPreferencesDataStoreFactory.kt") {
            // Skip the rule for the DefaultPreferencesDataStoreFactory implementation
            return
        }

        if (delegate.text.startsWith("by preferencesDataStore")) {
            report(CodeSmell(
                issue = issue,
                entity = Entity.from(delegate),
                message = "Use `PreferenceDataStoreFactory.create(name)` instead of `by preferencesDataStore(...)`."
            ))
        }
    }
}
