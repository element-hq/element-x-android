/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

interface CallAnalyticCredentialsProvider {
    val posthogUserId: String?
    val posthogApiHost: String?
    val posthogApiKey: String?
    val rageshakeSubmitUrl: String?
    val sentryDsn: String?
}
