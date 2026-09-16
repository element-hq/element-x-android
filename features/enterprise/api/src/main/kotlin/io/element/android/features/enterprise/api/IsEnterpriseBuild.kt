/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

/**
 * A use case to check if this build of the app is an enterprise build.
 */
fun interface IsEnterpriseBuild {
    /**
     * Returns `true` if this build of the app is an enterprise build, `false` otherwise.
     */
    operator fun invoke(): Boolean
}
