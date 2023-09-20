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

package io.element.android.features.call

import android.net.Uri
import javax.inject.Inject

class CallIntentDataParser @Inject constructor() {

    private val validHttpSchemes = sequenceOf("http", "https")

    fun parse(data: String?): String? {
        val parsedUrl = data?.let { Uri.parse(data) } ?: return null
        val scheme = parsedUrl.scheme
        return when {
            scheme in validHttpSchemes && parsedUrl.host == "call.element.io" -> data
            scheme == "element" && parsedUrl.host == "call" -> {
                // We use this custom scheme to load arbitrary URLs for other instances of Element Call,
                // so we can only verify it's an HTTP/HTTPs URL with a non-empty host
                parsedUrl.getUrlParameter()
            }
            scheme == "io.element.call" && parsedUrl.host == null -> {
                // We use this custom scheme to load arbitrary URLs for other instances of Element Call,
                // so we can only verify it's an HTTP/HTTPs URL with a non-empty host
                parsedUrl.getUrlParameter()
            }
            // This should never be possible, but we still need to take into account the possibility
            else -> null
        }
    }

    private fun Uri.getUrlParameter(): String? {
        return getQueryParameter("url")
            ?.takeIf {
                val internalUri = Uri.parse(it)
                internalUri.scheme in validHttpSchemes && !internalUri.host.isNullOrBlank()
            }
    }
}
