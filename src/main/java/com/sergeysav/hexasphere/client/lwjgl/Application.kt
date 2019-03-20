package com.sergeysav.hexasphere.client.lwjgl

import mu.KotlinLogging
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

/**
 * @author sergeys
 *
 * @constructor Creates a new Application
 */
abstract class Application(width: Int, height: Int) {
    private val log = KotlinLogging.logger {}
    
    protected var window: Long = MemoryUtil.NULL
        private set
    
    var width: Int = width
        private set
    
    var height: Int = height
        private set
    
    val keysDown = mutableSetOf<Int>()
    val mouseDown = mutableSetOf<Int>()
    
    fun run() {
        log.info { "LWJGL ${Version.getVersion()} Application Starting" }
        
        try {
            init(width, height, "Tesselation Test")
            loop()
        } catch (e: Exception) {
            log.error(e) { "Error Occurred" }
        } finally {
            log.debug { "Closing" }
            cleanup()
            Callbacks.glfwFreeCallbacks(window)
            GLFW.glfwDestroyWindow(window)
            GLFW.glfwTerminate()
            GLFW.glfwSetErrorCallback(null)?.free()
        }
    }
    
    private fun init(width: Int, height: Int, title: String) {
        log.debug { "Initializing" }
        GLFWErrorCallback.createPrint(System.err).set()
    
        if ( !GLFW.glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")
        
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
    
        //Request minimum OpenGL 3.3
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
    
        //Allow the usage of forward compatability
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
    
        //Get a core OpenGL profile
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
    
        //Don't show the winow as soon as it is created
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
    
        //Allow the window to be resized
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)
        
        log.trace { "Creating application" }
        create()
    
        log.trace { "Creating window" }
        window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL)
            throw IllegalStateException("Failed to create the GLFW window")
    
        log.trace { "Adding callbacks" }
        GLFW.glfwSetFramebufferSizeCallback(window) { _, w, h ->
            this.width = w
            this.height = h
            GL11.glViewport(0, 0, w, h)
            this.resize(w, h)
        }
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            } else {
                if (action == GLFW.GLFW_PRESS) {
                    keysDown.add(key)
                }
                onKeyPress(key, scancode, action, mods)
                if (action == GLFW.GLFW_RELEASE) {
                    keysDown.remove(key)
                }
            }
        }
        
        GLFW.glfwSetMouseButtonCallback(window) { window, button, action, mods ->
            if (action == GLFW.GLFW_PRESS) {
                mouseDown.add(button)
            }
            var x: Double = -1.0
            var y: Double = -1.0
            MemoryStack.stackPush().use { stack ->
                val pX = stack.mallocDouble(1)
                val pY = stack.mallocDouble(1)
                
                GLFW.glfwGetCursorPos(window, pX, pY)
                x = pX.get()
                y = pY.get()
            }
            onMouseAction(button, action, mods, x, y)
            if (action == GLFW.GLFW_RELEASE) {
                mouseDown.remove(button)
            }
        }
        
        GLFW.glfwSetCursorPosCallback(window) { window, xpos, ypos ->
            onMouseMove(xpos, ypos)
            mouseDown.forEach { onMouseDrag(it, xpos, ypos) }
        }
    
        log.trace { "Setting window contexts" }
        // Get the thread stack and push a new frame
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*
        
            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(window, pWidth, pHeight)
        
            // Get the resolution of the primary monitor
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        
            // Center the window
            GLFW.glfwSetWindowPos(
                    window,
                    (vidmode!!.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            )
        } // the stack frame is popped automatically
    
        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window)
    
        // Enable v-sync
        GLFW.glfwSwapInterval(1)
    
        // Make the window visible
        GLFW.glfwShowWindow(window)
        GLFW.glfwFocusWindow(window)
        
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities(true)
    
        log.debug { "Initializing application" }
        init()
    }
    
    private fun loop() {
        log.debug { "Starting Render Loop" }
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!GLFW.glfwWindowShouldClose(window)) {
            render()
    
            GLFW.glfwSwapBuffers(window) // swap the color bufferobjects
            
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents()
        }
        log.debug { "Ending Render Loop" }
    }
    
    abstract fun create()
    abstract fun init()
    abstract fun render()
    abstract fun cleanup()
    
    open fun resize(width: Int, height: Int) = Unit
    open fun onKeyPress(key: Int, scancode: Int, action: Int, mods: Int) = Unit
    open fun onMouseAction(button: Int, action: Int, mods: Int, xpos: Double, ypos: Double) = Unit
    open fun onMouseMove(xpos: Double, ypos: Double) = Unit
    open fun onMouseDrag(button: Int, xpos: Double, ypos: Double) = Unit
}