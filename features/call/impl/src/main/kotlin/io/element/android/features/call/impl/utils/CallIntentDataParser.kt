/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import android.net.Uri
import androidx.core.net.toUri
import dev.zacsweers.metro.Inject

@Inject
class CallIntentDataParser {
    private val validHttpSchemes = sequenceOf("https")
    private val knownHosts = sequenceOf(
        "call.element.io",
    )

    fun parse(data: String?): String? {
        val parsedUrl = data?.toUri() ?: return null
        val scheme = parsedUrl.scheme
        return when {
            scheme in validHttpSchemes -> parsedUrl
            scheme == "element" && parsedUrl.host == "call" -> {
                parsedUrl.getUrlParameter()
            }
            scheme == "io.element.call" && parsedUrl.host == null -> {
                parsedUrl.getUrlParameter()
            }
            // This should never be possible, but we still need to take into account the possibility
            else -> null
        }
            ?.takeIf { it.host in knownHosts }
            ?.withCustomParameters()
    }

    private fun Uri.getUrlParameter(): Uri? {
        return getQueryParameter("url")
            ?.let { urlParameter ->
                urlParameter.toUri().takeIf { uri ->
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
