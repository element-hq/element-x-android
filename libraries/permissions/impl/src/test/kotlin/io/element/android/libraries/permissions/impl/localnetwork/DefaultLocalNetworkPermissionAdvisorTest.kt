/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl.localnetwork

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.network.LocalNetworkAddressClassifier
import io.element.android.libraries.androidutils.network.LocalNetworkClassification
import io.element.android.libraries.permissions.test.FakePermissionStateProvider
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val LOCAL_URL = "https://matrix.corp.internal"
private const val PUBLIC_URL = "https://matrix.org"

class DefaultLocalNetworkPermissionAdvisorTest {
    private fun advisor(
        sdkInt: Int,
        permissionGranted: Boolean,
        classification: LocalNetworkClassification,
    ) = DefaultLocalNetworkPermissionAdvisor(
        classifier = FakeLocalNetworkAddressClassifier(classification),
        permissionStateProvider = FakePermissionStateProvider(permissionGranted = permissionGranted),
        buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(sdkInt),
    )

    @Test
    fun `returns false when SDK below 37`() = runTest {
        val result = advisor(sdkInt = 36, permissionGranted = false, classification = LocalNetworkClassification.LocalIp)
            .shouldRequestPermissionFor(LOCAL_URL)
        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when permission already granted`() = runTest {
        val result = advisor(sdkInt = 37, permissionGranted = true, classification = LocalNetworkClassification.LocalIp)
            .shouldRequestPermissionFor(LOCAL_URL)
        assertThat(result).isFalse()
    }

    @Test
    fun `returns false when URL classified as public IP`() = runTest {
        val result = advisor(sdkInt = 37, permissionGranted = false, classification = LocalNetworkClassification.PublicIp)
            .shouldRequestPermissionFor(PUBLIC_URL)
        assertThat(result).isFalse()
    }

    @Test
    fun `returns true when SDK 37, permission missing, URL is local`() = runTest {
        val result = advisor(sdkInt = 37, permissionGranted = false, classification = LocalNetworkClassification.LocalIp)
            .shouldRequestPermissionFor(LOCAL_URL)
        assertThat(result).isTrue()
    }

    @Test
    fun `returns false when SDK 37, permission missing, URL unresolvable`() = runTest {
        val result = advisor(sdkInt = 37, permissionGranted = false, classification = LocalNetworkClassification.Unresolvable)
            .shouldRequestPermissionFor(LOCAL_URL)
        assertThat(result).isFalse()
    }

    @Test
    fun `returns true when SDK above 37, permission missing, URL is local`() = runTest {
        val result = advisor(sdkInt = 38, permissionGranted = false, classification = LocalNetworkClassification.LocalIp)
            .shouldRequestPermissionFor(LOCAL_URL)
        assertThat(result).isTrue()
    }
}

private class FakeLocalNetworkAddressClassifier(
    private val classification: LocalNetworkClassification,
) : LocalNetworkAddressClassifier {
    override suspend fun classify(url: String): LocalNetworkClassification = classification
}
