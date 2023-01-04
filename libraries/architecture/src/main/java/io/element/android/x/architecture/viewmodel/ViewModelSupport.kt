package io.element.android.x.architecture.viewmodel

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin

fun viewModelSupportNode(buildContext: BuildContext, plugins: List<Plugin> = emptyList(), composable: @Composable (Modifier) -> Unit): Node =
    ViewModelSupportNode(buildContext, plugins, composable)

class ViewModelSupportNode(
    buildContext: BuildContext,
    plugins: List<Plugin> = emptyList(),
    private val composable: @Composable (Modifier) -> Unit,
) : Node(
    buildContext, plugins = plugins
), ViewModelStoreOwner, SavedStateRegistryOwner {

    private val viewModelSupport = ViewModelSupport(
        lifecycle,
        buildContext.savedStateMap?.get("SAVED_STATE_REGISTRY") as Bundle?,
    )

    override fun getViewModelStore(): ViewModelStore {
        return viewModelSupport.viewModelStore
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = viewModelSupport.savedStateRegistry

    @Composable
    override fun View(modifier: Modifier) {
        composable(modifier)
    }
}

private class ViewModelSupport(
    private val lifecycle: Lifecycle,
    private val initialSavedState: Bundle?,
    val defaultArgs: Bundle? = null,
) : ViewModelStoreOwner, HasDefaultViewModelProviderFactory, SavedStateRegistryOwner {

    private val viewModelStore = ViewModelStore()
    private val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)

    //Don't replace the initial saved state until we have at least started
    private var canSaveState: Boolean = false

    init {
        savedStateRegistryController.performAttach()

        // We copy the bundle because the `savedStateRegistryController` will modify it.
        // We don't want to modify `initialSavedState` since we may need to return that as our
        // state in `saveState`.
        savedStateRegistryController.performRestore(initialSavedState?.let { Bundle(it) })
        enableSavedStateHandles()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                canSaveState = true
            }

            override fun onDestroy(owner: LifecycleOwner) {
                viewModelStore.clear()
            }
        })
    }

    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return SavedStateViewModelFactory(null, this, defaultArgs)
    }

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        val extras = MutableCreationExtras()
        extras[SAVED_STATE_REGISTRY_OWNER_KEY] = this
        extras[VIEW_MODEL_STORE_OWNER_KEY] = this
        defaultArgs?.let { args ->
            extras[DEFAULT_ARGS_KEY] = args
        }
        return extras
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    fun saveState(): Bundle? {
        return if (canSaveState) {
            Bundle().also(savedStateRegistryController::performSave)
        } else {
            initialSavedState
        }
    }
}
