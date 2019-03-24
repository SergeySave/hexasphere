package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.client.Sync
import com.sergeysav.hexasphere.client.lwjgl.GLFWManager
import com.sergeysav.hexasphere.client.screen.HexasphereDisplayScreen
import com.sergeysav.hexasphere.client.screen.MainMenuScreen
import com.sergeysav.hexasphere.client.screen.Screen
import com.sergeysav.hexasphere.common.LinAlgPool
import mu.KotlinLogging
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import java.util.Deque
import java.util.LinkedList
import kotlin.random.Random

/**
 * @author sergeys
 *
 * @constructor Creates a new Hexasphere
 */
class Hexasphere(val seed: Long): GLFWManager(800, 600) {
    private val log = KotlinLogging.logger {}
    
    private var lastNano = 0L
    private var screenStack: Deque<Screen> = LinkedList()
    private val sync = Sync()
    val linAlgPool = LinAlgPool()
    
    override fun create() {}
    
    override fun init() {
        openScreen(MainMenuScreen())
    
        keyCallbacks.add(priority = 10) { key, _, action, _ ->
            if (key == GLFW.GLFW_KEY_G && action == GLFW.GLFW_RELEASE) {
                destroyScreen()
                openScreen(HexasphereDisplayScreen(linAlgPool, Random.nextLong()))
                true
            } else if (key == GLFW.GLFW_KEY_H && action == GLFW.GLFW_RELEASE) {
                destroyScreen()
                openScreen(MainMenuScreen())
                true
            } else {
                false
            }
        }
    }
    
    override fun render() {
        val now = System.nanoTime()
        val delta = ((now - lastNano) / 1.0e9)
        lastNano = now
    
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    
        screenStack.peek().render(delta)
    
        sync(60)
    }
    
    override fun cleanup() {
        screenStack.peek().unregister(this)
        while (screenStack.isNotEmpty()) {
            screenStack.pop().cleanup()
        }
    }
    
    fun openScreen(screen: Screen) {
        screenStack.peek()?.unregister(this)
        screenStack.push(screen)
        screen.register(this)
    }
    
    /**
     * Note this will NOT cause the screen to be cleaned up
     * In order to clean the screen up use destroyScreen
     *
     * If this is called from the current screen then the returned result should be this
     */
    fun popScreen(): Screen? {
        val screen: Screen? = screenStack.pop()
        screen?.unregister(this)
        screenStack.peek()?.register(this)
        return screen
    }
    
    /**
     * Note this WILL cause the screen to be cleaned up
     * In order to not clean the screen up use popScreen
     */
    fun destroyScreen() {
        val screen: Screen? = screenStack.pop()
        screen?.unregister(this)
        screenStack.peek()?.register(this)
        screen?.cleanup()
    }
}