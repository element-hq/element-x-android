/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

/**
 * Supplies the analytics and crash reporting credentials passed to the embedded Element Call widget.
 *
 * Every member is `null` when the corresponding service is not configured for this build, or when the user has not consented to analytics.
 */
interface CallAnalyticCredentialsProvider {
    /** The PostHog identifier of the current user, so call analytics can be tied to the same person as app analytics. */
    val posthogUserId: String?

    /** The PostHog host the widget should send its events to. */
    val posthogApiHost: String?

    /** The PostHog API key the widget should use. */
    val posthogApiKey: String?

    /** The URL the widget should submit its rageshakes to. */
    val rageshakeSubmitUrl: String?

    /** The Sentry DSN the widget should report its crashes to. */
    val sentryDsn: String?
}
