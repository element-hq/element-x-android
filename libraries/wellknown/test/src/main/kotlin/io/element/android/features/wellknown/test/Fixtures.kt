/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.CustomRecoveryPassphraseRequirements
import io.element.android.libraries.wellknown.api.ElementWellKnown

fun anElementWellKnown(
    registrationHelperUrl: String? = null,
    enforceElementPro: Boolean? = null,
    rageshakeUrl: String? = null,
    brandColor: String? = null,
    notificationSound: String? = null,
    identityProviderAppScheme: String? = null,
    customRecoveryPassphraseRequirements: CustomRecoveryPassphraseRequirements? = null,
) = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
    rageshakeUrl = rageshakeUrl,
    brandColor = brandColor,
    notificationSound = notificationSound,
    identityProviderAppScheme = identityProviderAppScheme,
    customRecoveryPassphraseRequirements = customRecoveryPassphraseRequirements,
)

fun aCustomRecoveryPassphraseRequirements(
    minCharacterCount: Int = 8,
) = CustomRecoveryPassphraseRequirements(
    minCharacterCount = minCharacterCount,
)
