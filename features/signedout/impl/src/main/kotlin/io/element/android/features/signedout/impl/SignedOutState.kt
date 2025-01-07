/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.impl

import io.element.android.libraries.sessionstorage.api.SessionData

// Do not use default value, so no member get forgotten in the presenters.
data class SignedOutState(
    val appName: String,
    val signedOutSession: SessionData?,
    val eventSink: (SignedOutEvents) -> Unit,
)
