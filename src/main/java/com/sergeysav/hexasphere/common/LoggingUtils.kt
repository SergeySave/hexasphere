package com.sergeysav.hexasphere.common

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

/**
 * @author sergeys
 */
object LoggingUtils {
    fun setLogLevel(level: org.slf4j.event.Level) {
        val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        rootLogger.level = Level.toLevel(level.name)
    }
}