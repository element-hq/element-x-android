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

package io.element.android.features.call.impl.utils

import android.net.Uri
import javax.inject.Inject

class CallIntentDataParser @Inject constructor() {
    private val validHttpSchemes = sequenceOf("https")

    fun parse(data: String?): String? {
        val parsedUrl = data?.let { Uri.parse(data) } ?: return null
        val scheme = parsedUrl.scheme
        return when {
            scheme in validHttpSchemes && parsedUrl.host == "call.element.io" -> parsedUrl
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
        }?.withCustomParameters()
    }

    private fun Uri.getUrlParameter(): Uri? {
        return getQueryParameter("url")
            ?.let { urlParameter ->
                Uri.parse(urlParameter).takeIf { uri ->
                    uri.scheme in validHttpSchemes && !uri.host.isNullOrBlank()
                }
            }
    }
}

/**
 * Ensure the uri has the following parameters and value in the fragment:
 * - appPrompt=false
 * - confineToRoom=true
 * to ensure that the rendering will bo correct on the embedded Webview.
 */
private fun Uri.withCustomParameters(): String {
    val builder = buildUpon()
    // Remove the existing query parameters
    builder.clearQuery()
    queryParameterNames.forEach {
        if (it == APP_PROMPT_PARAMETER || it == CONFINE_TO_ROOM_PARAMETER) return@forEach
        builder.appendQueryParameter(it, getQueryParameter(it))
    }
    // Remove the existing fragment parameters, and build the new fragment
    val currentFragment = fragment ?: ""
    // Reset the current fragment
    builder.fragment("")
    val queryFragmentPosition = currentFragment.lastIndexOf("?")
    val newFragment = if (queryFragmentPosition == -1) {
        // No existing query, build it.
        "$currentFragment?$APP_PROMPT_PARAMETER=false&$CONFINE_TO_ROOM_PARAMETER=true"
    } else {
        buildString {
            append(currentFragment.substring(0, queryFragmentPosition + 1))
            val queryFragment = currentFragment.substring(queryFragmentPosition + 1)
            // Replace the existing parameters
            val newQueryFragment = queryFragment
                .replace("$APP_PROMPT_PARAMETER=true", "$APP_PROMPT_PARAMETER=false")
                .replace("$CONFINE_TO_ROOM_PARAMETER=false", "$CONFINE_TO_ROOM_PARAMETER=true")
            append(newQueryFragment)
            // Ensure the parameters are there
            if (!newQueryFragment.contains("$APP_PROMPT_PARAMETER=false")) {
                if (newQueryFragment.isNotEmpty()) {
                    append("&")
                }
                append("$APP_PROMPT_PARAMETER=false")
            }
            if (!newQueryFragment.contains("$CONFINE_TO_ROOM_PARAMETER=true")) {
                append("&$CONFINE_TO_ROOM_PARAMETER=true")
            }
        }
    }
    // We do not want to encode the Fragment part, so append it manually
    return builder.build().toString() + "#" + newFragment
}

private const val APP_PROMPT_PARAMETER = "appPrompt"
private const val CONFINE_TO_ROOM_PARAMETER = "confineToRoom"
