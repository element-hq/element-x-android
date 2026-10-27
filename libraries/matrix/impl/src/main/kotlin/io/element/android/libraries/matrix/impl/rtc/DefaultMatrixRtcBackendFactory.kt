/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.callnative.api.MatrixRtcBackendFactory
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.impl.RustMatrixClient
import org.matrix.rtc.MatrixBackend

/**
 * Gives the native call component its view of Matrix.
 *
 * Here because this is the only module in the app compiled against the Rust SDK, and the backend needs
 * a raw SDK client. No feature module ever sees the SDK, and the rule that no feature depends on
 * `libraries/matrix/impl` still holds.
 *
 * The backend is built on the client's own coroutine scope, which is what ties it to the session: the
 * component has no shutdown of its own, so cancelling that scope at logout is the only thing that stops
 * the RTC core and its to-device subscription.
 */
@ContributesBinding(AppScope::class)
class DefaultMatrixRtcBackendFactory(
    private val dispatchers: CoroutineDispatchers,
) : MatrixRtcBackendFactory {
    override fun create(client: MatrixClient): MatrixBackend {
        return SdkMatrixBackend(
            client = (client as RustMatrixClient).innerClient,
            sessionScope = client.sessionCoroutineScope,
            dispatchers = dispatchers,
        )
    }
}
