/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.IsEnterpriseBuild

@ContributesBinding(AppScope::class)
class DefaultIsEnterpriseBuild : IsEnterpriseBuild {
    override fun invoke(): Boolean = false
}
