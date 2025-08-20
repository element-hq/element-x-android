/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.matrix.api.SdkMetadata
import org.matrix.rustcomponents.sdk.sdkGitSha
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class RustSdkMetadata() : SdkMetadata {
    override val sdkGitSha: String
        get() = sdkGitSha()
}
