/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.toolbox.test.sdk

import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider

class FakeBuildVersionSdkIntProvider(
    private val sdkInt: Int
) : BuildVersionSdkIntProvider {
    override fun get(): Int = sdkInt
}
