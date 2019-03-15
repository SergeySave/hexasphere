package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Application
import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.GLDrawingMode
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.map.WorldRenderable
import com.sergeysav.hexasphere.map.gen.MapGenerationSettings
import com.sergeysav.hexasphere.map.gen.generate
import mu.KotlinLogging
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

/**
 * @author sergeys
 *
 * @constructor Creates a new Hexasphere
 */
class Hexasphere : Application(800, 600) {
    private val log = KotlinLogging.logger {}
    
    lateinit var cameraController: CameraController
    
    private var lastNano = 0L
    
    lateinit var renderer: Renderer
    lateinit var normalRenderer: NormalRenderer
    lateinit var stereographicRenderer: SimpleStereographicRenderer
    
    val linAlgPool = LinAlgPool()
    val mapGenerationSettings = MapGenerationSettings(31, 30, 0L,
                                                                                       8, 0.9f, 0.5f,
                                                                                       0.2f, 5f, 1f,
                                                                                       0f, 0.05f,
                                                                                       8, 0.3f, 0.5f,
                                                                                       8, 0.3f, 0.5f,
                                                                                       2, 0.8f, 1.2f, linAlgPool)
    
    val a = DoubleArray(1)
    val b = DoubleArray(1)
    val world = mapGenerationSettings.generate()
    lateinit var worldRenderable: WorldRenderable
    
    override fun create() {}
    
    override fun init() {
        // Set the clear color
        GL11.glClearColor(0.2f, 0.2f, 0.2f, 0.0f)
    
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT)
        GL11.glPointSize(8f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    
        cameraController = CameraController(Camera(Math.toRadians(45.0).toFloat(), width.toFloat() / height, 0.1f,
                        100f), linAlgPool)
        cameraController.setPos(2f, 0f, 0f)
        cameraController.lookAt(0f, 0f, 0f)
    
        log.info { "Creating Mesh" }
        val mesh = Mesh(GLDrawingMode.TRIANGLES, true)
    
        worldRenderable = WorldRenderable(world, Matrix4f(), mesh)
        
        worldRenderable.prepareMesh {
            normalRenderer = NormalRenderer(linAlgPool)
            stereographicRenderer = SimpleStereographicRenderer(linAlgPool)
        }
        renderer = normalRenderer
    }
    
    override fun render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
        
        val speed = (0.1f * Math.pow(cameraController.camera.position.length().toDouble() / 5, 1.5)).toFloat()
        
        val upDown = speed * (if (keysDown.contains(GLFW.GLFW_KEY_W)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_S)) -1 else 0)
        val rightLeft = speed * (if (keysDown.contains(GLFW.GLFW_KEY_D)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_A)) -1 else 0)
        val rotate = 0.075f * (if (keysDown.contains(GLFW.GLFW_KEY_E)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_Q)) -1 else 0)
        val inOut = speed * (if (keysDown.contains(GLFW.GLFW_KEY_SPACE)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_LEFT_SHIFT)) -1 else 0)
        
        GLFW.glfwGetCursorPos(window, a, b)
    
        val mouseoverTile = renderer.getMouseoverTile(2 * a[0].toFloat() / width - 1,
                                                      2 * b[0].toFloat() / height - 1,
                                                      worldRenderable, cameraController)
        
        worldRenderable.updateMesh(mouseoverTile)
        
        cameraController.run {
            setAspect(width, height)
            translate(forward, inOut)
            rotateAround(ZERO, right, -upDown)
            rotateAround(ZERO, up, rightLeft)
            rotate(forward, -rotate)
            
            if (camera.position.lengthSquared() < 1.25*1.25f) {
                camera.position.normalize(1.25f)
            }
            if (camera.position.lengthSquared() > 8*8) {
                camera.position.normalize(8f)
            }
            
            update()
        }
    
        val now = System.nanoTime()
//        val delta = 1 / ((now - lastNano) / 1.0e9)
        lastNano = now
        
        renderer.render(worldRenderable, cameraController.camera)
    }
    
    override fun cleanup() {
        stereographicRenderer.cleanup()
        normalRenderer.cleanup()
        worldRenderable.mesh.cleanup()
    }
    
    override fun onKeyPress(key: Int, scancode: Int, action: Int, mods: Int) {
        if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_R) {
            if (renderer == normalRenderer) {
                renderer = stereographicRenderer
            } else {
                renderer = normalRenderer
            }
        }
    }
}