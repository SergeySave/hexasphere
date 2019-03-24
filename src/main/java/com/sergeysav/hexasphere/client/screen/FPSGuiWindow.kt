package com.sergeysav.hexasphere.client.screen

import com.sergeysav.hexasphere.client.nuklear.Gui
import com.sergeysav.hexasphere.client.nuklear.GuiWindow
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
        
        nkEditing(gui) {
            window.background.hidden {
                window(0f, 0f, 100f, 28f,
                       Nuklear.NK_WINDOW_NO_INPUT or
                               Nuklear.NK_WINDOW_ROM or
                               Nuklear.NK_WINDOW_NO_SCROLLBAR) {
                    dynamicRow(30f, 1) {
                        label("FPS: ${(runningAverage.average() * 1).roundToInt()}")
                    }
                }
            }
        }
    }
}