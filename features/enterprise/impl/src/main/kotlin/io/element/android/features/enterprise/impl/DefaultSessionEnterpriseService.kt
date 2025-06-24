/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.enterprise.api.SessionEnterpriseService
import io.element.android.libraries.di.SessionScope
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultSessionEnterpriseService @Inject constructor() : SessionEnterpriseService {
    override suspend fun isElementCallAvailable(): Boolean = true
}
