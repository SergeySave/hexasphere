package com.sergeysav.hexasphere.client

import com.sergeysav.hexasphere.client.camera.Camera
import com.sergeysav.hexasphere.client.camera.CameraController
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.world.WorldRenderable
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.loadResource
import com.sergeysav.hexasphere.common.setUniform
import com.sergeysav.hexasphere.common.world.getClosestTileTo
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL20
import kotlin.math.min

/**
 * @author sergeys
 *
 * @constructor Creates a new NormalRenderer
 */
class NormalRenderer(val linAlgPool: LinAlgPool) : Renderer {
    val shader = ShaderProgram()
    private val lightDirection = Vector3f(1f, 1f, 1f).normalize()
    private val model = Matrix4f()
    
    init {
        shader.createVertexShader(loadResource("/shader/model.vertex.glsl"))
        shader.createFragmentShader(loadResource("/shader/model.fragment.glsl"))
        shader.link()
    }
    
    override fun render(worldRenderable: WorldRenderable, camera: Camera) {
        shader.bound {
            camera.combined.setUniform(shader.getUniform("uCamera"))
            GL20.glUniform3f(shader.getUniform("viewPos"), camera.position.x(), camera.position.y(), camera.position.z())
            GL20.glUniform3f(shader.getUniform("lightDir"), lightDirection.x(), lightDirection.y(), lightDirection.z())
            GL20.glUniform1f(shader.getUniform("ambientStrength"), 0.0f)
            model.setUniform(shader.getUniform("uModel"))
            worldRenderable.draw(shader)
        }
    }
    
    override fun getMouseoverTile(x: Float, y: Float, map: WorldRenderable,
                                  cameraController: CameraController): Tile? {
        return linAlgPool.vec3 {ray ->
            ray.set(linAlgPool.vec2 { cameraController.projectToWorld(it.set(x, y)) })
    
            val radius = 1
            linAlgPool.vec3 { v3 ->
                linAlgPool.vec3 { v ->
                    map.modelMatrix.getTranslation(v)
                    val part1 = -(ray.dot(v3.set(cameraController.camera.position).sub(v))).toDouble()
                    val det = part1*part1 - v3.set(cameraController.camera.position).sub(v).lengthSquared() + radius*radius
                    if (det >= 0) {
                        val part2 = Math.sqrt(det)
                        val dist = min(part1 - part2, part1 + part2)
                        v3.set(cameraController.camera.position).add(ray.mul(dist.toFloat()))
                
                        map.world.getClosestTileTo(v3, linAlgPool)
                    } else {
                        null
                    }
                }
            }
        }
    }
    
    override fun cleanup() {
        shader.cleanup()
    }
}