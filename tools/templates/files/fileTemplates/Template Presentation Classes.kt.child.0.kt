#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class ${NAME}StateProvider : PreviewParameterProvider<${NAME}State> {
    override val values: Sequence<${NAME}State>
        get() = sequenceOf(
            a${NAME}State(),
            // Add other states here
        )
}

fun a${NAME}State() = ${NAME}State(
    eventSink = {}
)
