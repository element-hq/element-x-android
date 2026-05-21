/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.live.service.LiveLocationReceiver
import io.element.android.features.location.impl.live.service.LiveLocationSharingCoordinator
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LiveLocationSharingCoordinatorTest {
    @Test
    fun `first registration starts the service and last unregister stops it`() = runTest {
        var startCount = 0
        var stopCount = 0
        val coordinator = LiveLocationSharingCoordinator(
            startService = { startCount++ },
            stopService = { stopCount++ },
            nowMillis = { 0L },
        )

        coordinator.register(A_SESSION_ID, LiveLocationReceiver { })
        coordinator.unregister(A_SESSION_ID)

        assertThat(startCount).isEqualTo(1)
        assertThat(stopCount).isEqualTo(1)
    }

    @Test
    fun `dispatch isolates receiver failures and still reaches later receivers`() = runTest {
        val delivered = mutableListOf<Location>()
        val coordinator = LiveLocationSharingCoordinator(
            startService = { },
            stopService = { },
            nowMillis = { 4_000L },
        )

        coordinator.register(A_SESSION_ID) { error("boom") }
        coordinator.register(A_SESSION_ID_2) { location -> delivered += location }
        coordinator.dispatch(Location(lat = 1.0, lon = 2.0, accuracy = 3f))

        assertThat(delivered).containsExactly(Location(lat = 1.0, lon = 2.0, accuracy = 3f))
    }

    @Test
    fun `dispatch delivers first location immediately`() = runTest {
        var nowMillis = 4_000L
        val delivered = mutableListOf<Location>()
        val coordinator = LiveLocationSharingCoordinator(
            startService = { },
            stopService = { },
            nowMillis = { nowMillis },
        )

        coordinator.register(A_SESSION_ID) { location -> delivered += location }

        val firstLocation = Location(lat = 1.0, lon = 2.0, accuracy = 3f)

        coordinator.dispatch(firstLocation)

        assertThat(delivered).containsExactly(firstLocation)
    }

    @Test
    fun `dispatch drops updates inside the throttle window`() = runTest {
        var nowMillis = 4_000L
        val delivered = mutableListOf<Location>()
        val coordinator = LiveLocationSharingCoordinator(
            startService = { },
            stopService = { },
            nowMillis = { nowMillis },
        )

        coordinator.register(A_SESSION_ID) { location -> delivered += location }

        val firstLocation = Location(lat = 1.0, lon = 2.0, accuracy = 3f)
        val secondLocation = Location(lat = 4.0, lon = 5.0, accuracy = 6f)

        coordinator.dispatch(firstLocation)
        nowMillis += 500
        coordinator.dispatch(secondLocation)

        assertThat(delivered).containsExactly(firstLocation)
    }

    @Test
    fun `dispatch delivers next update after the throttle window elapses`() = runTest {
        var nowMillis = 4_000L
        val delivered = mutableListOf<Location>()
        val coordinator = LiveLocationSharingCoordinator(
            startService = { },
            stopService = { },
            nowMillis = { nowMillis },
        )

        coordinator.register(A_SESSION_ID) { location -> delivered += location }

        val firstLocation = Location(lat = 1.0, lon = 2.0, accuracy = 3f)
        val secondLocation = Location(lat = 4.0, lon = 5.0, accuracy = 6f)

        coordinator.dispatch(firstLocation)
        nowMillis += 3_000
        coordinator.dispatch(secondLocation)

        assertThat(delivered).containsExactly(firstLocation, secondLocation).inOrder()
    }
}
