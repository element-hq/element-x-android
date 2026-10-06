/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope

@ContributesBinding(RoomScope::class)
class NoopSecurityAndPrivacyExtension : SecurityAndPrivacyExtension {
    override suspend fun isPublicAccessAllowed(): Boolean = true
}
