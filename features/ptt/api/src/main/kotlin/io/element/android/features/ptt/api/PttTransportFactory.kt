/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

/**
 * Creates the [PttTransport] for a channel, selecting the implementation from
 * [PttChannelConfig.transport].
 *
 * Each call returns a fresh, un-joined transport; the caller owns its lifecycle and must call
 * [PttTransport.leave] when done.
 */
interface PttTransportFactory {
    fun create(config: PttChannelConfig): PttTransport
}
