/*
 * Copyright (c) 2021 New Vector Ltd
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

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.file.EncryptedFileFactory
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import timber.log.Timber
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

private const val ROOMS_NOTIFICATIONS_FILE_NAME_LEGACY = "im.vector.notifications.cache"
private const val FILE_NAME = "notifications.bin"

private val loggerTag = LoggerTag("NotificationEventPersistence", LoggerTag.NotificationLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultNotificationEventPersistence @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationEventPersistence {
    private val file by lazy {
        deleteLegacyFileIfAny()
        context.getDatabasePath(FILE_NAME)
    }

    private val encryptedFile by lazy {
        EncryptedFileFactory(context).create(file)
    }

    override fun loadEvents(factory: (List<NotifiableEvent>) -> NotificationEventQueue): NotificationEventQueue {
        val rawEvents: ArrayList<NotifiableEvent>? = file
            .takeIf { it.exists() }
            ?.let {
                try {
                    encryptedFile.openFileInput().use { fis ->
                        ObjectInputStream(fis).use { ois ->
                            @Suppress("UNCHECKED_CAST")
                            ois.readObject() as? ArrayList<NotifiableEvent>
                        }
                    }.also {
                        Timber.tag(loggerTag.value).d("Deserializing ${it?.size} NotifiableEvent(s)")
                    }
                } catch (e: Throwable) {
                    Timber.tag(loggerTag.value).e(e, "## Failed to load cached notification info")
                    null
                }
            }
        return factory(rawEvents.orEmpty())
    }

    override fun persistEvents(queuedEvents: NotificationEventQueue) {
        Timber.tag(loggerTag.value).d("Serializing ${queuedEvents.rawEvents().size} NotifiableEvent(s)")
        // Always delete file before writing, or encryptedFile.openFileOutput() will throw
        file.safeDelete()
        if (queuedEvents.isEmpty()) return
        try {
            encryptedFile.openFileOutput().use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(queuedEvents.rawEvents())
                }
            }
        } catch (e: Throwable) {
            Timber.tag(loggerTag.value).e(e, "## Failed to save cached notification info")
        }
    }

    private fun deleteLegacyFileIfAny() {
        tryOrNull {
            File(context.applicationContext.cacheDir, ROOMS_NOTIFICATIONS_FILE_NAME_LEGACY).delete()
        }
    }
}
