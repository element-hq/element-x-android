/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.invitelist.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invitelist.api.SeenInvitesStore
import io.element.android.libraries.di.DefaultPreferences
import io.element.android.libraries.di.SessionScope
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultSeenInvitesStore @Inject constructor(
    @DefaultPreferences private val sharedPrefs: SharedPreferences,
) : SeenInvitesStore {

    override var seenRoomIds: Set<String>
        get() = sharedPrefs.getStringSet(PREFS_KEY_SEEN_INVITES, null) ?: emptySet()
        set(value) = sharedPrefs.edit { putStringSet(PREFS_KEY_SEEN_INVITES, value) }

    companion object {
        private const val PREFS_KEY_SEEN_INVITES = "SEEN_INVITES"
    }
}
