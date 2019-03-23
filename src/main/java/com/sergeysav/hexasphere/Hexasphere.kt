package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.client.NormalRenderer
import com.sergeysav.hexasphere.client.Renderer
import com.sergeysav.hexasphere.client.SimpleStereographicRenderer
import com.sergeysav.hexasphere.client.Sync
import com.sergeysav.hexasphere.client.camera.Camera
import com.sergeysav.hexasphere.client.camera.CameraController
import com.sergeysav.hexasphere.client.gl.GLDrawingMode
import com.sergeysav.hexasphere.client.gl.Image
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.Texture2D
import com.sergeysav.hexasphere.client.gl.createTexture
import com.sergeysav.hexasphere.client.lwjgl.Application
import com.sergeysav.hexasphere.client.nuklear.FPSGuiWindow
import com.sergeysav.hexasphere.client.world.WorldRenderable
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.ZERO
import com.sergeysav.hexasphere.common.getResourcePath
import com.sergeysav.hexasphere.common.world.gen.MapGenerationSettings
import com.sergeysav.hexasphere.common.world.gen.generate
import mu.KotlinLogging
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

/**
 * @author sergeys
 *
 * @constructor Creates a new Hexasphere
 */
class Hexasphere(seed: Long) : Application(800, 600) {
    private val log = KotlinLogging.logger {}
    
    lateinit var cameraController: CameraController
    
    private var lastNano = 0L
    
    lateinit var renderer: Renderer
    lateinit var normalRenderer: NormalRenderer
    lateinit var stereographicRenderer: SimpleStereographicRenderer
    
    val linAlgPool = LinAlgPool()
    val mapGenerationSettings = MapGenerationSettings(31, 30, seed,
                                                      8, 0.8f, 0.5f,
                                                      8, 0.5f, 1.3f, 0.1f,
                                                      0.2f, 5f, 1f,
                                                      0.65, 0.05f,
                                                      8, 0.3f, 0.5f,
                                                      8, 0.3f, 0.5f,
                                                      2, 0.8f, 1.2f, linAlgPool)
    
    val a = DoubleArray(1)
    val b = DoubleArray(1)
    val world = mapGenerationSettings.generate()
    lateinit var worldRenderable: WorldRenderable
    var texture: Texture2D = Texture2D(0)
    private val sync = Sync()
    val fpsGuiWindow = FPSGuiWindow()
    
    override fun create() {}
    
    override fun init() {
        // Set the clear color
        GL11.glClearColor(0.2f, 0.2f, 0.2f, 0.0f)
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
        //        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
        //        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT)
        GL11.glPointSize(8f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        //        GL11.glEnable(GL11.GL_CULL_FACE)
    
        cameraController = CameraController(
                Camera(Math.toRadians(45.0).toFloat(), fWidth.toFloat() / fHeight,
                       0.1f,
                       100f), linAlgPool)
        cameraController.setPos(2f, 0f, 0f)
        cameraController.lookAt(0f, 0f, 0f)
    
        log.info { "Creating Mesh" }
        val mesh = Mesh(GLDrawingMode.TRIANGLES, true)
    
        texture = Image.createTexture(getResourcePath("/shapes/together.png"), GL11.GL_RGB, GL11.GL_RGB,
                                      generateMipmaps = true)
        
        worldRenderable = WorldRenderable(world, Matrix4f(), mesh, texture)
        
        worldRenderable.prepareMesh {
            normalRenderer = NormalRenderer(linAlgPool)
            stereographicRenderer = SimpleStereographicRenderer(linAlgPool)
        }
        renderer = normalRenderer
    
        keyCallbacks.add(priority = 0) { key, scancode, action, mods ->
            if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R) {
                if (renderer == normalRenderer) {
                    renderer = stereographicRenderer
                } else {
                    renderer = normalRenderer
                }
            }
            false
        }
    }
    
    override fun render() {
        val now = System.nanoTime()
        val delta = ((now - lastNano) / 1.0e9)
        lastNano = now
    
        fpsGuiWindow.layout(gui, 1 / delta)
        
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
        
        val speed = (0.1f * Math.pow(cameraController.camera.position.length().toDouble() / 5, 1.5)).toFloat()
    
        val upDown = speed * (if (GLFW.glfwGetKey(window,
                                                  GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) 1 else 0 + if (GLFW.glfwGetKey(
                        window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) -1 else 0) * delta * 60
        val rightLeft = speed * (if (GLFW.glfwGetKey(window,
                                                     GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) 1 else 0 + if (GLFW.glfwGetKey(
                        window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) -1 else 0) * delta * 60
        val rotate = 0.075f * (if (GLFW.glfwGetKey(window,
                                                   GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS) 1 else 0 + if (GLFW.glfwGetKey(
                        window, GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS) -1 else 0) * delta * 60
        val inOut = speed * (if (GLFW.glfwGetKey(window,
                                                 GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) 1 else 0 + if (GLFW.glfwGetKey(
                        window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) -1 else 0) * delta * 60

        cameraController.run {
            setAspect(fWidth, fHeight)
            translate(forward, inOut.toFloat())
            rotateAround(ZERO, right, -upDown.toFloat())
            rotateAround(ZERO, up, rightLeft.toFloat())
            rotate(forward, -rotate.toFloat())

            if (camera.position.lengthSquared() < 1.25*1.25f) {
                camera.position.normalize(1.25f)
            }
            if (camera.position.lengthSquared() > 8*8) {
                camera.position.normalize(8f)
            }
    
            update()
        }
    
        GLFW.glfwGetCursorPos(window, a, b)
        val mouseoverTile = renderer.getMouseoverTile(2 * a[0].toFloat() / width - 1,
                                                      2 * b[0].toFloat() / height - 1,
                                                      worldRenderable, cameraController)
    
        worldRenderable.updateMesh(mouseoverTile)
    
        renderer.render(worldRenderable, cameraController.camera)
    
        sync(60)
    }
    
    override fun cleanup() {
        stereographicRenderer.cleanup()
        normalRenderer.cleanup()
        worldRenderable.mesh.cleanup()
        texture.cleanup()
    }
}