package com.sergeysav.hexasphere.client.screen

import com.sergeysav.hexasphere.Hexasphere
import mu.KotlinLogging
import org.lwjgl.opengl.GL11

/**
 * @author sergeys
 */
class MainMenuScreen: Screen {
    private val log = KotlinLogging.logger {}
    private lateinit var application: Hexasphere
    private var mainMenuScreenWindow: MainMenuScreenWindow? = null
    
    init {
        log.trace { "Creating Main Menu Screen" }
    }
    
    override fun register(application: Hexasphere) {
        log.trace { "Registering Main Menu Screen" }
        this.application = application
        mainMenuScreenWindow = MainMenuScreenWindow()
        GL11.glClearColor(0.2f, 0.4f, 0.2f, 0.0f)
    }
    
    override fun render(delta: Double) {
        val mainMenuScreenWindow = mainMenuScreenWindow!!
        mainMenuScreenWindow.layout(application.gui, application.width.toFloat(), application.height.toFloat(),
                                    application)
    }
    
    override fun unregister(application: Hexasphere) {
        log.trace { "Unregistering Main Menu Screen" }
        mainMenuScreenWindow = null
    }
    
    override fun cleanup() {
        log.trace { "Cleaning up Main Menu Screen" }
    }
    
}