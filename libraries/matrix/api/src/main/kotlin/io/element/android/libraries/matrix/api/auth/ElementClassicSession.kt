/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import io.element.android.libraries.matrix.api.core.UserId

data class ElementClassicSession(
    val userId: UserId,
    val homeserverUrl: String?,
    val secrets: String?,
    val roomKeysVersion: String?,
    val doesContainBackupKey: Boolean,
)
