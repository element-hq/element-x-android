package io.element.android.x.features.roomlist.model


/**
 * Create a list of 16 RoomListRoomSummary placeholders
 */
fun createFakePlaceHolders(): List<RoomListRoomSummary> {
    return mutableListOf<RoomListRoomSummary>().apply {
        for (i in 0..15) {
            add(RoomListRoomSummary.placeholder("\$fakeRoom$i"))
        }
    }
}
