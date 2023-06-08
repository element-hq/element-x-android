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

package io.element.android.features.login.impl.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.element.android.libraries.core.data.tryOrNull

fun openLearnMorePage(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LoginConstants.SLIDING_SYNC_READ_MORE_URL))
    tryOrNull { context.startActivity(intent) }
}
