#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class ${NAME}Presenter @Inject constructor() : Presenter<${NAME}State> {

    @Composable
    override fun present(): ${NAME}State {

        fun handleEvents(event: ${NAME}Events) {
            when (event) {
                ${NAME}Events.MyEvent -> Unit
            }
        }

        return ${NAME}State(
            eventSink = ::handleEvents
        )
    }
}
