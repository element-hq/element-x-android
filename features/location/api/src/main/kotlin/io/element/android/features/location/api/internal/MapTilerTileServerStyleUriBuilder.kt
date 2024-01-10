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

@file:JvmName("TileServerStyleUriBuilderKt")

package io.element.android.features.location.api.internal

import android.content.Context

internal class MapTilerTileServerStyleUriBuilder(
    private val apiKey: String,
    private val lightMapId: String,
    private val darkMapId: String,
) : TileServerStyleUriBuilder {

    constructor(context: Context) : this(
        apiKey = context.apiKey,
        lightMapId = context.mapId(darkMode = false),
        darkMapId = context.mapId(darkMode = true),
    )

    override fun build(darkMode: Boolean): String {
        val mapId = if (darkMode) darkMapId else lightMapId
        return "$MAPTILER_BASE_URL/$mapId/style.json?key=$apiKey"
    }
}
