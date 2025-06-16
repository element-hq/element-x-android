/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.pushers

import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustPushersServiceTest {
    @Test
    fun `setPusher should invoke the client method`() = runTest {
        val sut = RustPushersService(
            client = FakeFfiClient(),
            dispatchers = testCoroutineDispatchers()
        )
        sut.setHttpPusher(
            setHttpPusherData = aSetHttpPusherData()
        ).getOrThrow()
    }

    @Test
    fun `unsetPusher should invoke the client method`() = runTest {
        val sut = RustPushersService(
            client = FakeFfiClient(),
            dispatchers = testCoroutineDispatchers()
        )
        sut.unsetHttpPusher(
            unsetHttpPusherData = aUnsetHttpPusherData(),
        ).getOrThrow()
    }
}

private fun aSetHttpPusherData(
    pushKey: String = "pushKey",
    appId: String = "appId",
    url: String = "url",
    defaultPayload: String = "defaultPayload",
    appDisplayName: String = "appDisplayName",
    deviceDisplayName: String = "deviceDisplayName",
    profileTag: String = "profileTag",
    lang: String = "lang",
) = SetHttpPusherData(
    pushKey = pushKey,
    appId = appId,
    url = url,
    defaultPayload = defaultPayload,
    appDisplayName = appDisplayName,
    deviceDisplayName = deviceDisplayName,
    profileTag = profileTag,
    lang = lang
)

private fun aUnsetHttpPusherData(
    pushKey: String = "pushKey",
    appId: String = "appId",
) = UnsetHttpPusherData(
    pushKey = pushKey,
    appId = appId,
)
