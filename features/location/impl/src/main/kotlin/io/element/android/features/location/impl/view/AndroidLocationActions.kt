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

package io.element.android.features.location.impl.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.location.api.Location
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidLocationActions @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers
) : LocationActions {

    private var activityContext: Context? = null

    @Composable
    override fun Configure() {
        val context = LocalContext.current
        return DisposableEffect(Unit) {
            activityContext = context
            onDispose {
                activityContext = null
            }
        }
    }

    override suspend fun share(location: Location, label: String?) {
        runCatching {
            // Ref: https://developer.android.com/guide/components/intents-common#ViewMap
            val suffix = if (label != null) "(${Uri.encode(label)})" else ""
            val uri = Uri.parse("geo:0,0?q=${location.lat},${location.lon}$suffix")
            val showMapsIntent = Intent(Intent.ACTION_VIEW).setData(uri)
            val chooserIntent = Intent.createChooser(showMapsIntent, null)
            withContext(coroutineDispatchers.main) {
                activityContext!!.startActivity(chooserIntent)
            }
        }.onSuccess {
            Timber.v("Open location succeed")
        }.onFailure {
            Timber.e(it, "Open location failed")
        }
    }
}
