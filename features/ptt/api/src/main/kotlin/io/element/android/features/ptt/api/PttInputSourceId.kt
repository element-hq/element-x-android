/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

/**
 * Stable identifier for a hardware/accessory PTT input source. Doubles as the multibinding key
 * ([io.element.android.features.ptt.api.di.PttInputSourceKey]) and the id persisted in settings, so
 * these value names must stay stable. New sources (OEM rugged keys, generic hardware keys, BLE
 * pucks) add a value here.
 */
enum class PttInputSourceId {
    /** Bluetooth / wired accessories that emulate media keys (Pryme/AINA-style), via MediaSession. */
    MediaButton,
}
