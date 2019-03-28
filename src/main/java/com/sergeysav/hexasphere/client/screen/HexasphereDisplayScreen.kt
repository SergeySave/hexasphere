package com.sergeysav.hexasphere.client.screen

import com.sergeysav.hexasphere.Hexasphere
import com.sergeysav.hexasphere.client.NormalRenderer
import com.sergeysav.hexasphere.client.Renderer
import com.sergeysav.hexasphere.client.SimpleStereographicRenderer
import com.sergeysav.hexasphere.client.camera.Camera
import com.sergeysav.hexasphere.client.camera.CameraController
import com.sergeysav.hexasphere.client.gl.GLDrawingMode
import com.sergeysav.hexasphere.client.gl.Image
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.createTexture
import com.sergeysav.hexasphere.client.world.WorldRenderable
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.ZERO
import com.sergeysav.hexasphere.common.getResourcePath
import com.sergeysav.hexasphere.common.world.World
import com.sergeysav.hexasphere.common.world.gen.MapGenerationSettings
import com.sergeysav.hexasphere.common.world.gen.generate
import com.sergeysav.hexasphere.common.world.tile.Tile
import mu.KotlinLogging
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack

/**
 * @author sergeys
 *
 * @constructor Creates a new HexasphereDisplayScreen
 */
class HexasphereDisplayScreen(val linAlgPool: LinAlgPool, seed: Long): Screen {
    private val log = KotlinLogging.logger {}
    private lateinit var application: Hexasphere
    private lateinit var keyCallback: (Int, Int, Int, Int) -> Boolean
    private lateinit var mouseCallback: (Int, Int, Int, Double, Double) -> Boolean
    
    private val cameraController: CameraController
    private val worldRenderable: WorldRenderable
    private val world: World
    private val normalRenderer: NormalRenderer
    private val stereographicRenderer: SimpleStereographicRenderer
    private var renderer: Renderer
    private val mapGenerationSettings = MapGenerationSettings(31, 30, seed,
                                                              8, 0.8f, 0.5f,
                                                              8, 0.5f, 1.3f, 0.1f,
                                                              0.2f, 5f, 1f,
                                                              0.65, 0.05f,
                                                              8, 0.3f, 0.5f,
                                                              8, 0.3f, 0.5f,
                                                              2, 0.8f, 1.2f, linAlgPool)
    private val fpsGuiWindow = FPSGuiWindow()
    private val hexSelectedWindow: HexSelectedWindow
    private var selectedTile: Tile? = null
    private var mouseWasDown: Boolean = false
    private var mouseRelease: Boolean = false
    
    
    init {
        log.trace { "Creating Hexasphere Display Screen" }
        
        cameraController = CameraController(
                Camera(Math.toRadians(45.0).toFloat(), 1f, 0.1f, 100f), linAlgPool)
        cameraController.setPos(2f, 0f, 0f)
        cameraController.lookAt(0f, 0f, 0f)
        
        log.trace { "Creating Hexasphere Mesh" }
        val mesh = Mesh(GLDrawingMode.TRIANGLES, true)
        
        val texture = Image.createTexture(getResourcePath("/shapes/together.png"), GL11.GL_RGB, GL11.GL_RGB,
                                          generateMipmaps = true)
        
        world = mapGenerationSettings.generate()
        worldRenderable = WorldRenderable(world, Matrix4f(), mesh, texture)
    
        normalRenderer = NormalRenderer(linAlgPool)
        stereographicRenderer = SimpleStereographicRenderer(linAlgPool)
    
        worldRenderable.prepareMesh {
            normalRenderer.shaderProgram.validate()
            stereographicRenderer.shaderProgram.validate()
        }
    
        renderer = normalRenderer
    
        hexSelectedWindow = HexSelectedWindow(linAlgPool, worldRenderable)
    }
    
    override fun register(application: Hexasphere) {
        log.trace { "Registering Hexasphere Display Screen" }
        this.application = application
        GL11.glClearColor(0.2f, 0.2f, 0.2f, 0.0f)
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
        GL11.glPointSize(8f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_CULL_FACE)
        
        keyCallback = application.keyCallbacks.add(priority = 0) { key, _, action, _ ->
            if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_ESCAPE) {
                selectedTile = null
            }
            if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R) {
                if (renderer == normalRenderer) {
                    renderer = stereographicRenderer
                } else {
                    renderer = normalRenderer
                }
            }
            false
        }
        mouseCallback = application.mouseButtonCallbacks.add(priority = 0) { button, action, mods, x, y ->
            if (action == GLFW.GLFW_RELEASE && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (mouseWasDown) {
                    mouseRelease = true
                }
            }
            false
        }
    }
    
    override fun render(delta: Double) {
        fpsGuiWindow.layout(application.gui, 1 / delta)
        
        val speed = (0.1f * Math.pow(cameraController.camera.position.length().toDouble() / 5, 1.5)).toFloat()
        
        val upDown = speed * (if (application.isKeyPressed(GLFW.GLFW_KEY_W)) 1 else 0 + if (application.isKeyPressed(
                        GLFW.GLFW_KEY_S)) -1 else 0) * delta * 60
        val rightLeft = speed * (if (application.isKeyPressed(GLFW.GLFW_KEY_D)) 1 else 0 + if (application.isKeyPressed(
                        GLFW.GLFW_KEY_A)) -1 else 0) * delta * 60
        val rotate = 0.075f * (if (application.isKeyPressed(GLFW.GLFW_KEY_E)) 1 else 0 + if (application.isKeyPressed(
                        GLFW.GLFW_KEY_Q)) -1 else 0) * delta * 60
        val inOut = speed * (if (application.isKeyPressed(GLFW.GLFW_KEY_SPACE)) 1 else 0 + if (application.isKeyPressed(
                        GLFW.GLFW_KEY_LEFT_SHIFT)) -1 else 0) * delta * 60
        
        cameraController.run {
            setAspect(application.fWidth, application.fHeight)
            translate(forward, inOut.toFloat())
            rotateAround(ZERO, right, -upDown.toFloat())
            rotateAround(ZERO, up, rightLeft.toFloat())
            rotate(forward, -rotate.toFloat())
            
            if (camera.position.lengthSquared() < 1.25 * 1.25f) {
                camera.position.normalize(1.25f)
            }
            if (camera.position.lengthSquared() > 8 * 8) {
                camera.position.normalize(8f)
            }
            
            update()
        }
        
        val mouseoverTile = MemoryStack.stackPush().use {
            val x = it.doubles(0.0)
            val y = it.doubles(0.0)
            application.getCursorPosition(x, y)
            
            renderer.getMouseoverTile(2 * x[0].toFloat() / application.width - 1,
                                      2 * y[0].toFloat() / application.height - 1,
                                      worldRenderable, cameraController)
        }
    
        if (mouseRelease) {
            selectedTile = mouseoverTile
        }
        hexSelectedWindow.layout(application.gui, application.width.toFloat(),
                                 application.height.toFloat(), selectedTile)
        
        worldRenderable.updateMesh(mouseoverTile)
        
        renderer.render(worldRenderable, cameraController.camera)
    
        mouseRelease = false
        mouseWasDown = application.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)
    }
    
    override fun unregister(application: Hexasphere) {
        log.trace { "Unregistering Hexasphere Display Screen" }
        application.keyCallbacks.remove(keyCallback)
        application.mouseButtonCallbacks.remove(mouseCallback)
    }
    
    override fun cleanup() {
        log.trace { "Cleaning up Hexasphere Display Screen" }
        worldRenderable.mesh.cleanup()
        worldRenderable.texture.cleanup()
        normalRenderer.cleanup()
        stereographicRenderer.cleanup()
    }
}