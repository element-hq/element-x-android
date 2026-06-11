/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.wellknown.api.CustomRecoveryPassphrase
import io.element.android.libraries.wellknown.api.ElementWellKnown
import timber.log.Timber

private val loggerTag = LoggerTag("Wellknown")

/**
 * Floor for the recovery passphrase minimum length. A value of 1 guarantees the derived passphrase is
 * never empty (an empty passphrase derives a recoverable but effectively unprotected secret-storage
 * key). Anything stronger than non-empty is left to the homeserver operator's configuration.
 */
private const val MINIMUM_PASSPHRASE_CHARACTER_COUNT = 1

internal fun InternalElementWellKnown.map() = ElementWellKnown(
    registrationHelperUrl = registrationHelperUrl,
    enforceElementPro = enforceElementPro,
    rageshakeUrl = rageshakeUrl,
    brandColor = brandColor,
    notificationSound = notificationSound,
    identityProviderAppScheme = identityProviderAppScheme,
    customRecoveryPassphrase = customRecoveryPassphrase?.toPublic(),
)

private fun InternalCustomRecoveryPassphrase.toPublic(): CustomRecoveryPassphrase {
    // Whenever the homeserver advertises the settings block we run the custom passphrase flow, flooring
    // the minimum at 1 so the passphrase can never be empty even if the server omits min_character_count
    // or advertises a non-positive value. The operator owns any stronger minimum.
    val min = (minCharacterCount ?: MINIMUM_PASSPHRASE_CHARACTER_COUNT).coerceAtLeast(MINIMUM_PASSPHRASE_CHARACTER_COUNT)
    if (min != minCharacterCount) {
        Timber.tag(loggerTag.value).w(
            "custom_recovery_passphrase.min_character_count was %s; flooring to %d",
            minCharacterCount,
            min,
        )
    }
    return CustomRecoveryPassphrase(minCharacterCount = min)
}
