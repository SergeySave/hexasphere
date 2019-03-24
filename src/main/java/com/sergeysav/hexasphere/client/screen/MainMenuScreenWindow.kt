package com.sergeysav.hexasphere.client.screen

import com.sergeysav.hexasphere.Hexasphere
import com.sergeysav.hexasphere.client.nuklear.Gui
import com.sergeysav.hexasphere.client.nuklear.GuiWindow
import com.sergeysav.hexasphere.client.nuklear.HAlign
import org.lwjgl.nuklear.Nuklear
import kotlin.random.Random


/**
 * @author sergeys
 *
 * @constructor Creates a new FPSGuiWindow
 */
class MainMenuScreenWindow: GuiWindow("MainMenuScreen") {
    
    fun layout(gui: Gui, width: Float, height: Float, hexasphere: Hexasphere) {
        nkEditing(gui) {
            window.windowRect?.w(width)
            window.windowRect?.h(height)
            window.background.hidden {
                window(0f, 0f, width, height,
                       Nuklear.NK_WINDOW_NO_SCROLLBAR) {
                    font(gui.bigFont) {
                        dynamicRow(100f, 1) {
                            label("Hexasphere", HAlign.CENTER)
                        }
                        dynamicRow(50f, 1) {
                            button("Play") {
                                hexasphere.destroyScreen()
                                hexasphere.openScreen(HexasphereDisplayScreen(hexasphere.linAlgPool, Random.nextLong()))
                            }
                        }
                    }
                }
            }
        }
    }
}