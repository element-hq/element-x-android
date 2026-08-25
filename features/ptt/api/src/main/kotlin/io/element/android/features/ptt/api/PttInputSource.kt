/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

/**
 * A source of hardware/accessory push-to-talk press/release signals — a media-button accessory, an
 * OEM rugged key, a BLE puck, and so on. Sources are owned by the PTT session host for the life of a
 * session and all funnel to the same transmit path via the lambdas passed to [start].
 *
 * The lambda shape intentionally mirrors the existing accessory controllers so folding them in is
 * mechanical.
 */
interface PttInputSource {
    val id: PttInputSourceId

    /** Begin listening; invoke [onPressToTalk] on button down and [onReleaseToTalk] on button up. */
    fun start(onPressToTalk: () -> Unit, onReleaseToTalk: () -> Unit)

    /** Stop listening and release any system resources (media sessions, receivers, GATT connections). */
    fun stop()
}
