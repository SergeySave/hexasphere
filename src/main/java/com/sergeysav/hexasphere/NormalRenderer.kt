package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.ShaderProgram
import com.sergeysav.hexasphere.gl.bound
import com.sergeysav.hexasphere.map.Map
import com.sergeysav.hexasphere.map.tile.Tile
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.min

/**
 * @author sergeys
 *
 * @constructor Creates a new NormalRenderer
 */
class NormalRenderer : Renderer {
    val shaderProgram = ShaderProgram()
    private val v2 = Vector2f()
    
    init {
        shaderProgram.createVertexShader(loadResource("/vertex.glsl"))
        shaderProgram.createFragmentShader(loadResource("/fragment.glsl"))
        shaderProgram.link()
    }
    
    override fun render(mesh: Mesh, model: Matrix4f, camera: Camera) {
        shaderProgram.bound {
            camera.combined.setUniform(shaderProgram.getUniform("uCamera"))
            model.setUniform(shaderProgram.getUniform("uModel"))
            mesh.draw()
        }
    }
    
    override fun getMouseoverTile(x: Float, y: Float, map: Map, model: Matrix4f,
                                  cameraController: CameraController): Tile? {
        v2.set(x, y)
        val ray = cameraController.projectToWorld(v2)
    
        val radius = 1
        val v3 = Vector3f()
        val v = Vector3f()
        model.getTranslation(v)
        val part1 = -(ray.dot(v3.set(cameraController.camera.position).sub(v))).toDouble()
        val det = part1*part1 - v3.set(cameraController.camera.position).sub(v).lengthSquared() + radius*radius
        var closest: Tile? = null
        if (det >= 0) {
            val part2 = Math.sqrt(det)
            val dist = min(part1 - part2, part1 + part2)
            v3.set(cameraController.camera.position).add(ray.mul(dist.toFloat()))
    
            var minDist2 = Float.MAX_VALUE
            for (tile in map.tiles) {
                tile.getCenter(v)
                val d2 = v.sub(v3).lengthSquared()
                if (d2 < minDist2) {
                    minDist2 = d2
                    closest = tile
                }
            }
        }
        return closest
    }
    
    override fun cleanup() {
        shaderProgram.cleanup()
    }
}