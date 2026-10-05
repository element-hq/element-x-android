/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import io.element.android.libraries.matrix.api.search.SearchBackfillStrategy
import uniffi.matrix_sdk.SearchBackfillStrategy as RustSearchBackfillStrategy

fun RustSearchBackfillStrategy.map(): SearchBackfillStrategy = when (this) {
    RustSearchBackfillStrategy.FOREGROUND -> SearchBackfillStrategy.FOREGROUND
    RustSearchBackfillStrategy.BACKGROUND -> SearchBackfillStrategy.BACKGROUND
}

fun SearchBackfillStrategy.map(): RustSearchBackfillStrategy = when (this) {
    SearchBackfillStrategy.FOREGROUND -> RustSearchBackfillStrategy.FOREGROUND
    SearchBackfillStrategy.BACKGROUND -> RustSearchBackfillStrategy.BACKGROUND
}
