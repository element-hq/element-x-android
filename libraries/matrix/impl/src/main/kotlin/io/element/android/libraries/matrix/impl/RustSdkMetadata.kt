/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.SdkMetadata
import org.matrix.rustcomponents.sdk.sdkGitSha
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RustSdkMetadata @Inject constructor() : SdkMetadata {
    override val sdkGitSha: String
        get() = sdkGitSha()
}
