/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.detektrules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression

class RunCatchingRule(config: Config) : Rule(config) {
    override val issue: Issue = Issue(
        id = "RunCatchingNotAllowed",
        severity = Severity.Style,
        description = "Avoid using `runCatching`, use `runCatchingExceptions` or `tryOrNull` instead. " +
            "Avoid `mapCatching`, use `mapCatchingExceptions` instead.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val callNameExpression = expression.getCallNameExpression() ?: return
        val hasRunCatchingCall = callNameExpression.text == "runCatching"
        val hasMapCatchingCall = callNameExpression.text == "mapCatching"
        if (hasRunCatchingCall || hasMapCatchingCall) {
            report(CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = "Use `runCatchingExceptions` or `tryOrNull` instead of `runCatching`. Avoid `mapCatching`, use `mapCatchingExceptions` instead."
            ))
        }
    }
}
