package com.sergeysav.hexasphere.client.nuklear

import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear

/**
 * @author sergeys
 *
 * @constructor Creates a new GuiWindow
 */
abstract class GuiWindow(val windowName: String) {
    private var windowRect: NkRect? = null
    
    fun window(gui: Gui, x: Float, y: Float, w: Float, h: Float, windowOptions: Int, inner: () -> Unit) {
        if (windowRect == null) {
            // Create the rectangle that represents
            windowRect = NkRect.create()
            Nuklear.nk_rect(x, y, w, h, windowRect!!)
            
            // Draw the window just once to create it
            Nuklear.nk_begin(gui.context, windowName, windowRect!!, windowOptions)
            Nuklear.nk_end(gui.context)
        } else if (!Nuklear.nk_window_is_closed(gui.context, windowName)) {
            if (Nuklear.nk_begin(gui.context, windowName, windowRect!!, windowOptions)) {
                inner()
            }
            Nuklear.nk_end(gui.context)
        }
    }
}