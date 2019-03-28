package com.sergeysav.hexasphere.client.gl

import com.sergeysav.hexasphere.client.Bindable
import org.lwjgl.opengl.GL33C

/**
 * @author sergeys
 *
 * @constructor Creates a new Framebuffer
 */
inline class Framebuffer(val id: Int): Bindable {
    override fun bind() {
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, id)
    }
    
    override fun unbind() {
        GL33C.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, 0)
    }
}
