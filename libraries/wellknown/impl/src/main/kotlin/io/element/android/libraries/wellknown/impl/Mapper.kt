/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphraseRequirements
import io.element.android.libraries.wellknown.api.ElementWellKnown
import timber.log.Timber

private val loggerTag = LoggerTag("Wellknown")

internal fun InternalElementWellKnown.map() = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
    rageshakeUrl = rageshakeUrl,
    brandColor = brandColor,
    notificationSound = notificationSound,
    identityProviderAppScheme = identityProviderAppScheme,
    customRecoveryPassphraseRequirements = customRecoveryPassphraseRequirements?.toPublic(),
)

private fun InternalCustomRecoveryPassphraseRequirements.toPublic(): CustomRecoveryPassphraseRequirements? {
    val min = minCharacterCount ?: run {
        Timber.tag(loggerTag.value).w("custom_recovery_passphrase_settings missing min_character_count; ignoring spec")
        return null
    }
    if (min <= 0) {
        Timber.tag(loggerTag.value).w("custom_recovery_passphrase_settings.min_character_count must be > 0; ignoring spec")
        return null
    }
    return CustomRecoveryPassphraseRequirements(minCharacterCount = min)
}
