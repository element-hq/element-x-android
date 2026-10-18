/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.api

import io.element.android.call.api.matrix.ElementCallMatrixTransport
import io.element.android.libraries.matrix.api.MatrixClient

/**
 * Makes the native call's view of Matrix for a session.
 *
 * The component's turnkey transport is built over a raw Rust SDK client, and only
 * `libraries/matrix/impl` is compiled against the SDK. This is the seam that keeps it that way: the
 * implementation lives there, everything else takes a [MatrixClient] and asks.
 *
 * A factory rather than a binding for the transport itself, because the call is started from app scope
 * - a call can be placed for an account that is not the one on screen - and the session it belongs to
 * is known only as an id until a client is fetched for it.
 */
fun interface ElementCallTransportFactory {
    fun create(client: MatrixClient): ElementCallMatrixTransport
}
