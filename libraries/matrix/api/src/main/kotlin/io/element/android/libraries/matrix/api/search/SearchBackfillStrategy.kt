/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

enum class SearchBackfillStrategy {
    /**
     * The app is in the foreground: pause between paginations so this doesn't
     * compete with interactive traffic.
     */
    FOREGROUND,

    /**
     * A time-boxed background task (e.g. iOS `BGAppRefreshTask`) where there's
     * no interactive traffic to protect.
     */
    BACKGROUND
}
