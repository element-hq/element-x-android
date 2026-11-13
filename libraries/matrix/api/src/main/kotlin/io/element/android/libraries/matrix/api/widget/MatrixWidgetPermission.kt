/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

/** A native-consent description of an SDK capability, including its exact event restrictions. */
data class MatrixWidgetPermission(
    val kind: Kind,
    val eventType: String? = null,
    // Null means any state key; an empty string is a specific (empty) state key.
    val stateKey: String? = null,
    val messageType: String? = null,
) {
    enum class Kind {
        ReadMessages,
        SendMessages,
        ReadState,
        SendState,
        ReadToDevice,
        SendToDevice,
        SendDelayedEvents,
        UpdateDelayedEvents,
        DownloadFiles,
        DiscoverRtcTransports,
        ObtainLivekitToken,
        DelegateLivekitLeave,
    }
}
