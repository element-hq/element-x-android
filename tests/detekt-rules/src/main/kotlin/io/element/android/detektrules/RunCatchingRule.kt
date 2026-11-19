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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression

class RunCatchingRule(config: Config) : Rule(
    config = config,
    description = "Avoid using `runCatching`, use `runCatchingExceptions` or `tryOrNull` instead. " +
        "Avoid `mapCatching`, use `mapCatchingExceptions` instead.",
) {
    override val ruleName = RuleName("RunCatchingNotAllowed")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val callNameExpression = expression.getCallNameExpression() ?: return
        val hasRunCatchingCall = callNameExpression.text == "runCatching"
        val hasMapCatchingCall = callNameExpression.text == "mapCatching"
        if (hasRunCatchingCall || hasMapCatchingCall) {
            report(
                Finding(
                    entity = Entity.from(expression),
                    message = "Use `runCatchingExceptions` or `tryOrNull` instead of `runCatching`. Avoid `mapCatching`, use `mapCatchingExceptions` instead."
                )
            )
        }
    }
}
