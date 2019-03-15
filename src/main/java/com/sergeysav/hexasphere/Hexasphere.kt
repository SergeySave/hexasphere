package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Application
import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.GLDataUsage
import com.sergeysav.hexasphere.gl.GLDrawingMode
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.Vec3VertexAttribute
import com.sergeysav.hexasphere.map.World
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
    
    lateinit var vertices: FloatArray
    lateinit var indices: IntArray
    
    lateinit var mesh: Mesh
    lateinit var cameraController: CameraController
    var matrix: Matrix4f = Matrix4f()
    
    var mouseX: Double = 0.0
    var mouseY: Double = 0.0
    private var ignore: Boolean = false
    private var count = 2
    private var lastNano = 0L
    
    lateinit var renderer: Renderer
    lateinit var normalRenderer: NormalRenderer
    lateinit var stereographicRenderer: SimpleStereographicRenderer
    
    val mapGenerationSettings = MapGenerationSettings(31, 30, 0L,
                                                                                       8, 0.9f, 0.5f,
                                                                                       0.2f, 5f, 1f,
                                                                                       0f, 0.05f,
                                                                                       8, 0.3f, 0.5f,
                                                                                       8, 0.3f, 0.5f,
                                                                                       2, 0.8f, 1.2f)
    
    val a = DoubleArray(1)
    val b = DoubleArray(1)
    lateinit var map: World
    
    override fun create() {
        map = mapGenerationSettings.generate()
    
        vertices = FloatArray(6 * map.numVertices)
        indices = IntArray(3 * map.numTriangles)
    
        map.apply {
            for (i in 0 until numPentagons) {
                val verts = tiles[i].tilePolygon.vertices
                val biome = tiles[i].biome
                for (j in 0 until verts.size) {
                    vertices[5 * 6 * i + 6 * j + 0] = verts[j].x()
                    vertices[5 * 6 * i + 6 * j + 1] = verts[j].y()
                    vertices[5 * 6 * i + 6 * j + 2] = verts[j].z()
                    vertices[5 * 6 * i + 6 * j + 3] = biome.r
                    vertices[5 * 6 * i + 6 * j + 4] = biome.g
                    vertices[5 * 6 * i + 6 * j + 5] = biome.b
                }
                for (j in 2 until verts.size) {
                    indices[(5 - 2) * 3 * i + 3 * (j - 2) + 0] = 5 * i
                    indices[(5 - 2) * 3 * i + 3 * (j - 2) + 1] = 5 * i + j - 1
                    indices[(5 - 2) * 3 * i + 3 * (j - 2) + 2] = 5 * i + j
                }
            }
            val hexVOffset = numPentagons * 5
            val hexIOffset = (5 - 2) * 3 * numPentagons
            for (i in 0 until numHexagons) {
                val verts = tiles[i + numPentagons].tilePolygon.vertices
                val biome = tiles[i + numPentagons].biome
                for (j in 0 until verts.size) {
                    vertices[6 * 6 * i + 6 * j + 0 + 6 * hexVOffset] = verts[j].x()
                    vertices[6 * 6 * i + 6 * j + 1 + 6 * hexVOffset] = verts[j].y()
                    vertices[6 * 6 * i + 6 * j + 2 + 6 * hexVOffset] = verts[j].z()
                    vertices[6 * 6 * i + 6 * j + 3 + 6 * hexVOffset] = biome.r
                    vertices[6 * 6 * i + 6 * j + 4 + 6 * hexVOffset] = biome.g
                    vertices[6 * 6 * i + 6 * j + 5 + 6 * hexVOffset] = biome.b
                }
                for (j in 2 until verts.size) {
                    indices[(6 - 2) * 3 * i + 3 * (j - 2) + 0 + hexIOffset] = 6 * i + hexVOffset
                    indices[(6 - 2) * 3 * i + 3 * (j - 2) + 1 + hexIOffset] = 6 * i + j - 1 + hexVOffset
                    indices[(6 - 2) * 3 * i + 3 * (j - 2) + 2 + hexIOffset] = 6 * i + j + hexVOffset
                }
            }
        }
        
        matrix.identity()
    }
    
    override fun init() {
        // Set the clear color
        GL11.glClearColor(0.2f, 0.2f, 0.2f, 0.0f)
    
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
//        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT)
        GL11.glPointSize(8f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    
        cameraController = CameraController(Camera(Math.toRadians(45.0).toFloat(), width.toFloat() / height, 0.1f,
                        100f))
        cameraController.setPos(2f, 0f, 0f)
        cameraController.lookAt(0f, 0f, 0f)
    
        log.info { "Creating Mesh" }
        mesh = Mesh(GLDrawingMode.TRIANGLES, true)
        mesh.setVertices(vertices, GLDataUsage.DYNAMIC,
                         Vec3VertexAttribute("aPos"),
                         Vec3VertexAttribute("aColor"))
        
        mesh.bound {
            normalRenderer = NormalRenderer()
            stereographicRenderer = SimpleStereographicRenderer()
        }
        renderer = normalRenderer
        
        mesh.setIndexData(indices, GLDataUsage.STATIC)
    }
    
    override fun render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
        
        val speed = (0.1f * Math.pow(cameraController.camera.position.length().toDouble() / 5, 1.5)).toFloat()
        
        val upDown = speed * (if (keysDown.contains(GLFW.GLFW_KEY_W)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_S)) -1 else 0)
        val rightLeft = speed * (if (keysDown.contains(GLFW.GLFW_KEY_D)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_A)) -1 else 0)
        val rotate = 0.075f * (if (keysDown.contains(GLFW.GLFW_KEY_E)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_Q)) -1 else 0)
        val inOut = speed * (if (keysDown.contains(GLFW.GLFW_KEY_SPACE)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_LEFT_SHIFT)) -1 else 0)
        
        GLFW.glfwGetCursorPos(window, a, b)
    
        val mouseoverTile = renderer.getMouseoverTile(2 * a[0].toFloat() / width - 1, 2 * b[0].toFloat() / height - 1,
                                                      map, matrix, cameraController)
        
        map.apply {
            for (i in 0 until numPentagons) {
                val biome = tiles[i].biome
                for (j in 0 until tiles[i].tilePolygon.polygonType.vertices) {
                    if (mouseoverTile == tiles[i]) {
                        vertices[5 * 6 * i + 6 * j + 3] = 1.0f
                        vertices[5 * 6 * i + 6 * j + 4] = 0.0f
                        vertices[5 * 6 * i + 6 * j + 5] = 0.0f
                    } else {
                        vertices[5 * 6 * i + 6 * j + 3] = biome.r
                        vertices[5 * 6 * i + 6 * j + 4] = biome.g
                        vertices[5 * 6 * i + 6 * j + 5] = biome.b
                    }
                }
            }
            val hexVOffset = numPentagons * 5
            for (i in 0 until numHexagons) {
                val biome = tiles[i + numPentagons].biome
                for (j in 0 until tiles[i + numPentagons].tilePolygon.polygonType.vertices) {
                    if (mouseoverTile == tiles[i + numPentagons]) {
                        vertices[6 * 6 * i + 6 * j + 3 + 6 * hexVOffset] = 1.0f
                        vertices[6 * 6 * i + 6 * j + 4 + 6 * hexVOffset] = 0.0f
                        vertices[6 * 6 * i + 6 * j + 5 + 6 * hexVOffset] = 0.0f
                    } else {
                        vertices[6 * 6 * i + 6 * j + 3 + 6 * hexVOffset] = biome.r
                        vertices[6 * 6 * i + 6 * j + 4 + 6 * hexVOffset] = biome.g
                        vertices[6 * 6 * i + 6 * j + 5 + 6 * hexVOffset] = biome.b
                    }
                }
            }
        }
        mesh.setVertexData(vertices, GLDataUsage.DYNAMIC)
        
        
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
        val delta = 1 / ((now - lastNano) / 1.0e9)
        lastNano = now
        
        renderer.render(mesh, matrix, cameraController.camera)
    }
    
    override fun cleanup() {
        stereographicRenderer.cleanup()
        normalRenderer.cleanup()
        mesh.cleanup()
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
    
    override fun onMouseAction(button: Int, action: Int, mods: Int, xpos: Double, ypos: Double) {
//        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
//
//        }
    }
    
//    override fun onMouseAction(button: Int, action: Int, mods: Int, x: Double, y: Double) {
//        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
//            mouseX = x
//            mouseY = y
//            if (action == GLFW.GLFW_PRESS) {
//                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)
//                count = 2
//            } else if (action == GLFW.GLFW_RELEASE) {
//                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL)
//            }
//        }
//    }
//
//    override fun onMouseDrag(button: Int, xpos: Double, ypos: Double) {
//        if (!ignore && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
//            if (count > 0) {
//                count--
//                return
//            }
//            val dX = xpos - mouseX
//            val dY = -ypos + mouseY
//            mouseX = xpos
//            mouseY = ypos
//            val rotAmt = Math.sqrt(dX * dX + dY * dY) / 100
//            val axis = Vector3f().set(camera.right).mul(dX.toFloat())
//                    .add(Vector3f().set(camera.up).mul(dY.toFloat()))
//                    .cross(camera.direction).normalize()
//            camera.rotate(rotAmt.toFloat(), axis)
//        }
//    }
    
    inline infix fun <reified A, reified  B, reified  C> ((A)->B).then(crossinline f: (B) -> C): (A) -> C {
        return { x -> f(this(x)) }
    }
}