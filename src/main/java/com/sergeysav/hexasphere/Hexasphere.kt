package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Application
import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.GLDataUsage
import com.sergeysav.hexasphere.gl.GLDrawingMode
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.Vec3VertexAttribute
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
import org.joml.SimplexNoise
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.random.Random


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
    
    val map = createBaseMap(31)
    var seed = 0f
    
    override fun create() {
        val numVertices = map.tiles.asSequence().map(Tile::type).map(TileType::vertices).sum()
        vertices = FloatArray(6 * numVertices)
        val numTriangles = map.tiles.asSequence().map(Tile::type).map { it.vertices - 2 }.sum()
        indices = IntArray(3 * numTriangles)
    
        val random = Random(0L)
        val tectonicPlates = map.generateTectonicPlates(30, random)
        var elevations = generateElevations(tectonicPlates)
        elevations = elevations.erode(0f, 0.05f)
        // "blur" elevations
        val heat = map.generateHeat(elevations, noiseGenerator(random.nextFloat()*1e4.toFloat(), octaves = 8, aScaling = 0.3f))
        val moisture = map.generateMoisture(noiseGenerator(random.nextFloat()*1e4.toFloat(), octaves = 8, aScaling = 0.3f))
        val biomes = map.generateBiomes(elevations, heat, moisture, noiseGenerator(random.nextFloat()*1e4.toFloat(), octaves = 2, aScaling = 0.8f, fScaling = 1.2f))

//        val min = moisture.values.min()!!
//        val max = moisture.values.max()!!
//        val diff = max - min
//        val adjust: (Float)->Float = { (it - min)/diff }
    
//        log.info { min }
//        log.info { max }
    
        var v = 0
        var i = 0
        val verts = Array(6) { Vector3f() }
        val vector = Vector3f()
    
        for ((plateNum, plate) in tectonicPlates.withIndex()) {
            for (tile in plate.tiles) {
                tile.getCenter(verts[0])
                val color = computeType(verts[0], seed)
                val num = tile.getVertices(verts)
                val vertexNum = v / 6
    
                val isBoundary = plate.boundaryTiles.contains(tile)
                val biome = biomes[tile]!!
    
                for (j in 0 until num) {
                    vertices[v++] = verts[j].x
                    vertices[v++] = verts[j].y
                    vertices[v++] = verts[j].z
                    vertices[v++] = biome.r//if (moisture[tile]!! <= 0.65*heat[tile]!!) 1.0f else 0.0f
                    vertices[v++] = biome.g//if (moisture[tile]!! >= 0.65*heat[tile]!!) 1.0f else 0.0f
                    vertices[v++] = biome.b//0.0f
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
        cameraController.setPos(5f, 0f, 0f)
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
        
        val upDown = 0.1f * (if (keysDown.contains(GLFW.GLFW_KEY_W)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_S)) -1 else 0)
        val rightLeft = 0.1f * (if (keysDown.contains(GLFW.GLFW_KEY_D)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_A)) -1 else 0)
        val rotate = 0.1f * (if (keysDown.contains(GLFW.GLFW_KEY_E)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_Q)) -1 else 0)
        val inOut = 0.1f * (if (keysDown.contains(GLFW.GLFW_KEY_SPACE)) 1 else 0 + if (keysDown.contains(GLFW.GLFW_KEY_LEFT_SHIFT)) -1 else 0)
        
        cameraController.run {
            setAspect(width, height)
            translate(forward, inOut)
            rotateAround(ZERO, right, -upDown)
            rotateAround(ZERO, up, rightLeft)
            rotate(forward, -rotate)
            
            if (camera.position.lengthSquared() < 1.25*1.25f) {
                camera.position.normalize(1.25f)
            }
            if (camera.position.lengthSquared() > 25.95*25.95f) {
                camera.position.normalize(25.95f)
            }
            
            update()
        }
    
        val now = System.nanoTime()
        val delta = 1 / ((now - lastNano) / 1.0e9)
        lastNano = now
        
        renderer.render(mesh, matrix, cameraController.camera)
        
//        seed += 1f
//        val center = Vector3f()
//        var v = 0
//        for (tile in map.tiles) {
//            tile.getCenter(center)
//            val color = computeType(center, seed)
//            val num = tile.type.vertices
//            for (j in 0 until num) {
//                v += 3
//                vertices[v++] = color.x
//                vertices[v++] = color.y
//                vertices[v++] = color.z
//            }
//        }
//        mesh.setVertexData(vertices, GLDataUsage.DYNAMIC)
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
    
    fun computeType(pos: Vector3f, seed: Float): Vector3f {
        val noise  = 0.125 * SimplexNoise.noise(pos.x * 2, pos.y * 2, pos.z * 2, seed) +
                     0.125 * SimplexNoise.noise(pos.x * 1.5f, pos.y * 1.5f, pos.z * 1.5f, seed) +
                     0.125 * SimplexNoise.noise(pos.x, pos.y, pos.z, seed) +
                     0.125 * SimplexNoise.noise(pos.x / 1.5f, pos.y / 1.5f, pos.z / 1.5f, seed) +
                     0.125 * SimplexNoise.noise(pos.x / 2, pos.y / 2, pos.z / 2, seed) +
                     0.125 * SimplexNoise.noise(pos.x / 2.5f, pos.y / 2.5f, pos.z / 2.5f, seed) +
                     0.125 * SimplexNoise.noise(pos.x / 3, pos.y / 3, pos.z / 3, seed) +
                     0.125 * SimplexNoise.noise(pos.x / 4, pos.y / 4, pos.z / 4, seed)
        if (noise >= 0) {
            return Vector3f(noise.toFloat(), max(1f - noise.toFloat(), noise.toFloat()), noise.toFloat())
        } else {
            return Vector3f(0f, 0f, 1 + noise.toFloat())
        }
    }
    
    fun noiseGenerator(seed: Float, octaves: Int = 1, aScaling: Float = 0.5f, fScaling: Float = 2.0f): (Vector3fc)->Float {
        val max = ((1.0 - Math.pow(aScaling.toDouble(), octaves.toDouble()))/(1 - aScaling)).toFloat()
        return { pos ->
            var total = 0f
            var amp = 1f
            var freq = 1f
            for (i in 0 until octaves) {
                total += amp * SimplexNoise.noise(pos.x() * freq,  pos.y() * freq,  pos.z() * freq,  seed.toFloat())
                amp *= aScaling
                freq *= fScaling
            }
            total / max
        }
    }
}