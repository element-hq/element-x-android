/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live.service

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultLiveLocationSharingCoordinatorTest {
    @Test
    fun `first registration starts the service and last unregister stops it`() = runTest {
        var startCount = 0
        var stopCount = 0
        val coordinator = DefaultLiveLocationSharingCoordinator(
            startService = { startCount++ },
            stopService = { stopCount++ },
        )

        coordinator.register(A_SESSION_ID, LiveLocationReceiver { })
        coordinator.unregister(A_SESSION_ID)

        assertThat(startCount).isEqualTo(1)
        assertThat(stopCount).isEqualTo(1)
    }

    @Test
    fun `dispatch isolates receiver failures and still reaches later receivers`() = runTest {
        val delivered = mutableListOf<Location>()
        val coordinator = DefaultLiveLocationSharingCoordinator(
            startService = { },
            stopService = { },
        )

        coordinator.register(A_SESSION_ID, LiveLocationReceiver { error("boom") })
        coordinator.register(A_SESSION_ID_2, LiveLocationReceiver { location -> delivered += location })

        coordinator.dispatch(Location(lat = 1.0, lon = 2.0, accuracy = 3f))

        assertThat(delivered).containsExactly(Location(lat = 1.0, lon = 2.0, accuracy = 3f))
    }
}
