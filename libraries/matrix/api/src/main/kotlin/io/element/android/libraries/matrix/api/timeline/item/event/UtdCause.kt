/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

enum class UtdCause {
    Unknown,
    SentBeforeWeJoined,
    VerificationViolation,
    UnsignedDevice,
    UnknownDevice,

    /**
     * Expected utd because this is a device-historical message and
     * key storage is not setup or not configured correctly.
     */
    HistoricalMessage,

    /**
     * The key was withheld on purpose because your device is insecure and/or the
     * sender trust requirement settings are not met for your device.
     */
    WithheldUnverifiedOrInsecureDevice,

    /**
     * Key is withheld by sender.
     */
    WithheldBySender,
}
