/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import org.matrix.rtc.FfiEventEncryption
import org.matrix.rustcomponents.sdk.EventEncryptionInfo
import org.matrix.rustcomponents.sdk.ShieldState

/** An event sent in the clear: no sending device to name, and no sender to confirm. */
internal val ClearText = FfiEventEncryption(encrypted = false, senderDeviceId = null, senderConfirmed = null)

/**
 * How the SDK says an event arrived; no encryption info is cleartext.
 *
 * Whether to vouch for the sender is the client's threat model, and the SDK has already applied it: an event
 * whose sender fails the client's `DecryptionSettings.senderDeviceTrustRequirement` is never delivered. Of
 * what remains, the client confirms the sender unless its shield is red.
 */
internal fun EventEncryptionInfo?.toFfi(): FfiEventEncryption = when (this) {
    null -> ClearText
    else -> FfiEventEncryption(encrypted = true, senderDeviceId = senderDeviceId, senderConfirmed = shieldState !is ShieldState.Red)
}
