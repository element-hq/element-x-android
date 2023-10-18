#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

// TODO Add your events or remove the file completely if no events
sealed interface ${NAME}Events {
    data object MyEvent: ${NAME}Events
}
