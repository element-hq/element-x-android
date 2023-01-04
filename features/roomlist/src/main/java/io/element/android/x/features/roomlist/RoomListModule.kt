package io.element.android.x.features.roomlist

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.element.android.x.architecture.AssistedNodeFactory
import io.element.android.x.architecture.NodeKey
import io.element.android.x.di.SessionScope

@Module
@ContributesTo(SessionScope::class)
abstract class RoomListModule {

    @Binds
    @IntoMap
    @NodeKey(RoomListNode::class)
    abstract fun bindRoomListNodeFactory(factory: RoomListNode.Factory): AssistedNodeFactory<*>
}
