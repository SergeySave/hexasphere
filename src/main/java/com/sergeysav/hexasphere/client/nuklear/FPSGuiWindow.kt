package com.sergeysav.hexasphere.client.nuklear

import org.lwjgl.nuklear.Nuklear
import kotlin.math.roundToInt


/**
 * @author sergeys
 *
 * @constructor Creates a new FPSGuiWindow
 */
class FPSGuiWindow: GuiWindow("FPS") {
    private var runningIndex = 0
    private val runningAverage = DoubleArray(5) { 0.0 }
    
    fun layout(gui: Gui, fps: Double) {
        runningAverage[runningIndex++] = fps
        runningIndex %= runningAverage.size
        
        Nuklear.nk_style_item_hide(gui.context.style().window().fixed_background())
        window(gui, 0f, 0f, 100f, 28f,
               Nuklear.NK_WINDOW_NO_INPUT or
                       Nuklear.NK_WINDOW_ROM or
                       Nuklear.NK_WINDOW_NO_SCROLLBAR
        ) {
            Nuklear.nk_layout_row_dynamic(gui.context, 30f, 1)
            Nuklear.nk_label(gui.context, "FPS: ${(runningAverage.average() * 1).roundToInt()}",
                             Nuklear.NK_TEXT_ALIGN_LEFT)
        }
    }
}