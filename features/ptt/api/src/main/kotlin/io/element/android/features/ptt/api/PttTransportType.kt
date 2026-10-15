/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

/**
 * The backend fulfilling a PTT channel.
 *
 * [ElementCall] is the first transport (MatrixRTC: E2EE, self-hostable, no extra server). [Zello]
 * and [Mumble] are the "bring your own server" transports from the requirements and are added behind
 * the same [PttTransport] seam later.
 */
enum class PttTransportType {
    ElementCall,
    Zello,
    Mumble,
}
