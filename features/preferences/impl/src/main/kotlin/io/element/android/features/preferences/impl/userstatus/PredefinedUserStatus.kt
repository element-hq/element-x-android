/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import androidx.annotation.StringRes
import io.element.android.features.preferences.impl.R

enum class PredefinedUserStatus(val emoji: String, @StringRes val labelRes: Int) {
    IN_A_MEETING("💬", R.string.common_user_status_in_a_meeting),
    FOCUS_TIME("💡", R.string.common_user_status_focus_time),
    ON_THE_ROAD("🚙", R.string.common_user_status_on_the_road),
    BE_RIGHT_BACK("☕", R.string.common_user_status_be_right_back),
    AWAY("🌴", R.string.common_user_status_away),
}
