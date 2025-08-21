/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.login.api.LoginUserStory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import dev.zacsweers.metro.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class DefaultLoginUserStory() : LoginUserStory {
    // True by default, will be set to false when the login user story is started, and set to true again once it's done.
    override val loginFlowIsDone: MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun setLoginFlowIsDone(value: Boolean) {
        loginFlowIsDone.value = value
    }
}
