/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import org.matrix.rustcomponents.sdk.SearchUsersResults
import org.matrix.rustcomponents.sdk.UserProfile

internal fun aRustSearchUsersResults(
    results: List<UserProfile>,
    limited: Boolean,
) = SearchUsersResults(
    results = results,
    limited = limited,
)
