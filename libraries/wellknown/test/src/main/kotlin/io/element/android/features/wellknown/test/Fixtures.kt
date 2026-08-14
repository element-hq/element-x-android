/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.CustomRecoveryPassphrase
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.MapTilerConfig

fun anElementWellKnown(
    registrationHelperUrl: String? = null,
    enforceElementPro: Boolean? = null,
    rageshakeUrl: String? = null,
    brandColor: String? = null,
    notificationSound: String? = null,
    identityProviderAppScheme: String? = null,
    customRecoveryPassphrase: CustomRecoveryPassphrase? = null,
    contentScannerUrl: String? = null,
    forceDisableE2EE: Boolean? = null,
    mapTilerConfig: MapTilerConfig? = null,
) = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
    rageshakeUrl = rageshakeUrl,
    brandColor = brandColor,
    notificationSound = notificationSound,
    identityProviderAppScheme = identityProviderAppScheme,
    customRecoveryPassphrase = customRecoveryPassphrase,
    contentScannerUrl = contentScannerUrl,
    forceDisableE2EE = forceDisableE2EE,
    mapTilerConfig = mapTilerConfig,
)

fun aCustomRecoveryPassphrase(
    minCharacterCount: Int = 8,
) = CustomRecoveryPassphrase(
    minCharacterCount = minCharacterCount,
)

fun aMapTilerConfig(
    apiKey: String = "test_api_key",
    baseUrl: String? = null,
    lightStyleId: String? = null,
    darkStyleId: String? = null,
) = MapTilerConfig(
    apiKey = apiKey,
    baseUrl = baseUrl,
    lightStyleId = lightStyleId,
    darkStyleId = darkStyleId,
)
