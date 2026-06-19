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

fun anElementWellKnown(
    registrationHelperUrl: String? = null,
    enforceElementPro: Boolean? = null,
    rageshakeUrl: String? = null,
    brandColor: String? = null,
    notificationSound: String? = null,
    identityProviderAppScheme: String? = null,
    customRecoveryPassphrase: CustomRecoveryPassphrase? = null,
) = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
    rageshakeUrl = rageshakeUrl,
    brandColor = brandColor,
    notificationSound = notificationSound,
    identityProviderAppScheme = identityProviderAppScheme,
    customRecoveryPassphrase = customRecoveryPassphrase,
)

fun aCustomRecoveryPassphrase(
    minCharacterCount: Int = 8,
) = CustomRecoveryPassphrase(
    minCharacterCount = minCharacterCount,
)
