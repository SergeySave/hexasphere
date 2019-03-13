package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Application
import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.GLDataUsage
import com.sergeysav.hexasphere.gl.GLDrawingMode
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.Vec3VertexAttribute
import com.sergeysav.hexasphere.map.Biome
import com.sergeysav.hexasphere.map.KMap
import com.sergeysav.hexasphere.map.MapGenerationSettings
import com.sergeysav.hexasphere.map.TectonicPlate
import com.sergeysav.hexasphere.map.createBaseMap
import com.sergeysav.hexasphere.map.erode
import com.sergeysav.hexasphere.map.generateBiomes
import com.sergeysav.hexasphere.map.generateElevations
import com.sergeysav.hexasphere.map.generateHeat
import com.sergeysav.hexasphere.map.generateMoisture
import com.sergeysav.hexasphere.map.generateTectonicPlates
import com.sergeysav.hexasphere.map.tile.Tile
import com.sergeysav.hexasphere.map.tile.TileType
import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
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
    val map = mapGenerationSettings.createBaseMap()
    var seed = 0f
    
    val v2 = Vector2f()
    val a = DoubleArray(1)
    val b = DoubleArray(1)
    lateinit var tectonicPlates: Array<TectonicPlate>
    lateinit var biomes: KMap<Tile, Biome>
    
    
    override fun create() {
        val numVertices = map.tiles.asSequence().map(Tile::type).map(TileType::vertices).sum()
        vertices = FloatArray(6 * numVertices)
        val numTriangles = map.tiles.asSequence().map(Tile::type).map { it.vertices - 2 }.sum()
        indices = IntArray(3 * numTriangles)
    
        // Plate Noise Generator
        tectonicPlates = mapGenerationSettings.generateTectonicPlates(map)
        var elevations = mapGenerationSettings.generateElevations(tectonicPlates)
        elevations = mapGenerationSettings.erode(elevations)
        // "blur" elevations
        val heat = mapGenerationSettings.generateHeat(map, elevations)
        val moisture = mapGenerationSettings.generateMoisture(map)
        biomes = mapGenerationSettings.generateBiomes(map, elevations, heat, moisture)
        
        var v = 0
        var i = 0
        val verts = Array(6) { Vector3f() }
        val vector = Vector3f()
    
        for ((plateNum, plate) in tectonicPlates.withIndex()) {
            for (tile in plate.tiles) {
                tile.vertexIndex = v
                tile.getCenter(verts[0])
                val num = tile.getVertices(verts)
                val vertexNum = v / 6
    
                val isBoundary = plate.boundaryTiles.contains(tile)
                val biome = biomes[tile]!!
    
                for (j in 0 until num) {
                    vertices[v++] = verts[j].x
                    vertices[v++] = verts[j].y
                    vertices[v++] = verts[j].z
                    vertices[v++] = biome.r
                    vertices[v++] = biome.g
                    vertices[v++] = biome.b
                }
                for (j in 2 until num) {
                    indices[i++] = vertexNum
                    indices[i++] = vertexNum + j - 1
                    indices[i++] = vertexNum + j
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
                                                      tectonicPlates, matrix, cameraController)
    
        for ((plateNum, plate) in tectonicPlates.withIndex()) {
            for (tile in plate.tiles) {
                var vert = tile.vertexIndex
                val biome = biomes[tile]!!
                for (j in 0 until tile.type.vertices) {
                    vert++
                    vert++
                    vert++
                    if (mouseoverTile == tile) {
                        vertices[vert++] = 1.0f
                        vertices[vert++] = 0.0f
                        vertices[vert++] = 0.0f
                    } else {
                        vertices[vert++] = biome.r
                        vertices[vert++] = biome.g
                        vertices[vert++] = biome.b
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