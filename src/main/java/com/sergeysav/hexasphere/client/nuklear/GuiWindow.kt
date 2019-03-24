package com.sergeysav.hexasphere.client.nuklear

import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkStyleItem
import org.lwjgl.nuklear.Nuklear
import org.lwjgl.system.MemoryStack

/**
 * @author sergeys
 *
 * @constructor Creates a new NkManager
 */
abstract class GuiWindow(val windowName: String) {
    private lateinit var gui: Gui
    private lateinit var stack: MemoryStack
    
    fun nkEditing(gui: Gui, inner: GuiWindow.() -> Unit) {
        MemoryStack.stackPush().use { stack ->
            this.stack = stack
            this.gui = gui
            this.inner()
        }
    }
    
    val window = Window()
    private val contents = Contents()
    
    inner class Window {
        val background = Background { gui.context.style().window().fixed_background() }
        var windowRect: NkRect? = null
            private set
        
        operator fun invoke(x: Float, y: Float, w: Float, h: Float, options: Int, inner: Contents.() -> Unit) {
            if (windowRect == null) {
                // Create the rectangle that represents
                windowRect = NkRect.create()
                Nuklear.nk_rect(x, y, w, h, windowRect!!)
                
                // Draw the window just once to create it
                Nuklear.nk_begin(gui.context, windowName, windowRect!!, options)
                Nuklear.nk_end(gui.context)
            } else if (!Nuklear.nk_window_is_closed(gui.context, windowName)) {
                if (Nuklear.nk_begin(gui.context, windowName, windowRect!!, options)) {
                    contents.inner()
                }
                Nuklear.nk_end(gui.context)
            }
        }
    }
    
    inner class Background(val item: () -> NkStyleItem) {
        fun hidden(inner: () -> Unit) {
            Nuklear.nk_style_push_style_item(gui.context,
                                             item(),
                                             Nuklear.nk_style_item_hide(item()))
            inner()
            Nuklear.nk_style_pop_style_item(gui.context)
        }
        //        fun color(r: Float, g: Float, b: Float, inner: () -> Unit) {
        //            Nuklear.nk_style_push_style_item(gui.context,
        //                                             item(),
        //                                             Nuklear.nk_style_item_color(NkColor.mallocStack(stack)
        //                                                                                 .r((r*255).toByte())
        //                                                                                 .g((g*255).toByte())
        //                                                                                 .b((b*255).toByte())
        //                                                                                 .a(255.toByte()), item()))
        //            inner()
        //            Nuklear.nk_style_pop_style_item(gui.context)
        //        }
    }
    
    inner class Contents {
        private val dynamicRow = DynamicRow()
        
        fun dynamicRow(height: Float, columns: Int, inner: DynamicRow.() -> Unit) {
            Nuklear.nk_layout_row_dynamic(gui.context, height, columns)
            dynamicRow.inner()
        }
        
        fun font(font: NkFont, inner: () -> Unit) {
            Nuklear.nk_style_push_font(gui.context, font.nkFont)
            inner()
            Nuklear.nk_style_pop_font(gui.context)
        }
    }
    
    inner class DynamicRow {
        fun label(text: String, hAlign: HAlign = HAlign.LEFT, vAlign: VAlign = VAlign.MIDDLE) {
            Nuklear.nk_label(gui.context, text, hAlign.id or vAlign.id)
        }
        
        fun button(text: String, inner: () -> Unit) {
            if (Nuklear.nk_button_label(gui.context, text)) {
                inner()
            }
        }
    }
}

enum class HAlign(val id: Int) {
    LEFT(Nuklear.NK_TEXT_ALIGN_LEFT),
    CENTER(Nuklear.NK_TEXT_ALIGN_CENTERED),
    RIGHT(Nuklear.NK_TEXT_ALIGN_RIGHT)
}

enum class VAlign(val id: Int) {
    TOP(Nuklear.NK_TEXT_ALIGN_TOP),
    MIDDLE(Nuklear.NK_TEXT_ALIGN_MIDDLE),
    BOTTOM(Nuklear.NK_TEXT_ALIGN_BOTTOM)
}
