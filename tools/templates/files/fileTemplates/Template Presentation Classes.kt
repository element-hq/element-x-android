#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import dev.zacsweers.metro.Inject

@Inject
class ${NAME}Presenter() : Presenter<${NAME}State> {

    @Composable
    override fun present(): ${NAME}State {

        fun handleEvent(event: ${NAME}Event) {
            when (event) {
                ${NAME}Event.MyEvent -> Unit
            }
        }

        return ${NAME}State(
            eventSink = ::handleEvent,
        )
    }
}
