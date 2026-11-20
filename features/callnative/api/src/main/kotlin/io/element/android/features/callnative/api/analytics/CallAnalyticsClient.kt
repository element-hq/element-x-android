/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.api.analytics

/** Where the call's analytics events are sent, while the user consents. */
interface CallAnalyticsClient {
    fun capture(event: String, properties: Map<String, Any>)

    /** Sends what is queued, then stops for good. */
    fun close()
}

/**
 * Makes the [CallAnalyticsClient], contributed into a set that is empty in builds without one: F-Droid builds must not
 * include PostHog, see https://github.com/element-hq/element-x-android/issues/7801
 */
fun interface CallAnalyticsClientFactory {
    /** Null when the build has no analytics project for calls. */
    fun create(): CallAnalyticsClient?
}
