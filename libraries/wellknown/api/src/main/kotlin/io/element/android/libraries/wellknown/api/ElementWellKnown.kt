/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

data class ElementWellKnown(
    val registrationHelperUrl: String?,
    val enforceElementPro: Boolean?,
    val rageshakeUrl: String?,
    val brandColor: String?,
    val notificationSound: String?,
    val identityProviderAppScheme: String?,
    val customRecoveryPassphrase: CustomRecoveryPassphrase?,
    val contentScannerUrl: String?,
    val forceDisableE2EE: Boolean?,
    val mapTilerConfig: MapTilerConfig?,
)

/**
 * A `MapTiler` configuration received from the server. This configuration is used to display maps in the app.
 */
data class MapTilerConfig(
    /** The API key to use with MapTiler. This is always not null, but can be empty/blank. */
    val apiKey: String,
    /** The style ID to use for light mode. This is optional and can be null. */
    val lightStyleId: String?,
    /** The style ID to use for light mode. This is optional and can be null. */
    val darkStyleId: String?,
    /** The base URL to use for MapTiler. This is optional and can be null. */
    val baseUrl: String?,
)
