/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context

// Mirrors the FileProvider authority declared in AndroidManifest.xml as
// `${applicationId}.fileprovider`. Kept here so the resolver's skip-rule and the copier's URI
// construction never drift apart.
internal const val NOTIFICATION_SOUND_FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

internal fun Context.notificationSoundFileProviderAuthority(): String =
    packageName + NOTIFICATION_SOUND_FILE_PROVIDER_AUTHORITY_SUFFIX
