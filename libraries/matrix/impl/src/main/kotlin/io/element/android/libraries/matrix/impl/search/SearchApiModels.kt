/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SearchRequestBody(
    @SerialName("search_categories")
    val searchCategories: SearchCategories,
)

@Serializable
internal data class SearchCategories(
    @SerialName("room_events")
    val roomEvents: RoomEventsSearchCriteria,
)

@Serializable
internal data class RoomEventsSearchCriteria(
    @SerialName("search_term")
    val searchTerm: String,
    val filter: RoomEventsFilter? = null,
    @SerialName("order_by")
    val orderBy: String? = null,
    val limit: Int? = null,
)

@Serializable
internal data class RoomEventsFilter(
    val rooms: List<String>? = null,
)

@Serializable
internal data class SearchResponseBody(
    @SerialName("search_categories")
    val searchCategories: SearchCategoriesResponse,
)

@Serializable
internal data class SearchCategoriesResponse(
    @SerialName("room_events")
    val roomEvents: RoomEventsSearchResponse? = null,
)

@Serializable
internal data class RoomEventsSearchResponse(
    val count: Long? = null,
    val highlights: List<String>? = null,
    val results: List<SearchResultItem>? = null,
    @SerialName("next_batch")
    val nextBatch: String? = null,
)

@Serializable
internal data class SearchResultItem(
    val rank: Double? = null,
    val result: SearchResultEvent? = null,
    val context: SearchEventContext? = null,
)

@Serializable
internal data class SearchResultEvent(
    @SerialName("event_id")
    val eventId: String? = null,
    val sender: String? = null,
    @SerialName("origin_server_ts")
    val originServerTs: Long? = null,
    val content: SearchEventContent? = null,
)

@Serializable
internal data class SearchEventContent(
    val body: String? = null,
    val msgtype: String? = null,
)

@Serializable
internal data class SearchEventContext(
    @SerialName("profile_info")
    val profileInfo: Map<String, SearchUserProfile>? = null,
)

@Serializable
internal data class SearchUserProfile(
    @SerialName("displayname")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)
