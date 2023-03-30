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

package io.element.android.x.intent

import android.content.Context
import android.content.Intent
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.x.MainActivity
import javax.inject.Inject

// TODO EAx change to deep-link.
@ContributesBinding(AppScope::class)
class IntentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : IntentProvider {
    override fun getMainIntent(): Intent {
        return Intent(context, MainActivity::class.java)
    }
}
