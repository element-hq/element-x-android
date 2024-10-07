/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultEnterpriseService @Inject constructor() : EnterpriseService {
    override val isEnterpriseBuild = false

    override suspend fun isEnterpriseUser(sessionId: SessionId) = false
}
