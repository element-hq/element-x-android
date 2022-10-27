package io.element.android.x.matrix

import android.util.Log
import io.element.android.x.matrix.store.SessionStore
import org.matrix.rustcomponents.sdk.*

class MatrixClient internal constructor(
    private val client: Client,
    private val sessionStore: SessionStore,
) {
    private val roomWrapper = RoomWrapper(client)

    fun startSync() {
        val clientDelegate = object : ClientDelegate {
            override fun didReceiveAuthError(isSoftLogout: Boolean) {
                Log.v(LOG_TAG, "didReceiveAuthError()")
            }

            override fun didReceiveSyncUpdate() {
                Log.v(LOG_TAG, "didReceiveSyncUpdate()")
            }

            override fun didUpdateRestoreToken() {
                Log.v(LOG_TAG, "didUpdateRestoreToken()")
            }
        }

        client.setDelegate(clientDelegate)
        Log.v(LOG_TAG, "DisplayName = ${client.displayName()}")
        try {
            client.fullSlidingSync()
        } catch (failure: Throwable) {
            Log.e(LOG_TAG, "fullSlidingSync() fail", failure)
        }
    }

    fun slidingSync(listener: SlidingSyncListener): StoppableSpawn {
        val slidingSyncView = SlidingSyncViewBuilder()
            .timelineLimit(limit = 10u)
            .requiredState(requiredState = listOf(RequiredState(key = "m.room.avatar", value = "")))
            .name(name = "HomeScreenView")
            .syncMode(mode = SlidingSyncMode.FULL_SYNC)
            .build()

        val slidingSync = client
            .slidingSync()
            .homeserver("https://slidingsync.lab.element.dev")
            .addView(slidingSyncView)
            .build()

        slidingSync.setObserver(object : SlidingSyncObserver {
            override fun didReceiveSyncUpdate(summary: UpdateSummary) {
                Log.v(LOG_TAG, "didReceiveSyncUpdate=$summary")
                val rooms = summary.rooms.mapNotNull {
                    roomWrapper.getRoom(it)
                }
                listener.onSyncUpdate(summary, rooms)
            }
        })
        return slidingSync.sync()
    }

    suspend fun logout() {
        client.logout()
        sessionStore.reset()
    }

    fun username(): String = client.displayName()
    fun avatarUrl(): String = client.avatarUrl()

    interface SlidingSyncListener {
        fun onSyncUpdate(summary: UpdateSummary, rooms: List<Room>)
    }
}
