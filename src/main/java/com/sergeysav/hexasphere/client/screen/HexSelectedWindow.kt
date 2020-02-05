package com.sergeysav.hexasphere.client.screen

import com.sergeysav.hexasphere.client.nuklear.Gui
import com.sergeysav.hexasphere.client.nuklear.GuiWindow
import com.sergeysav.hexasphere.client.world.TileToImageRenderer
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.lwjgl.nuklear.NkImage
import org.lwjgl.nuklear.Nuklear


/**
 * @author sergeys
 *
 * @constructor Creates a new FPSGuiWindow
 */
class HexSelectedWindow(linAlgPool: LinAlgPool): GuiWindow("Selected Hex") {
    
    val tileToImageRenderer = TileToImageRenderer(256, 256, linAlgPool)
    //    val nkImage: NkImage = N
    
    fun layout(gui: Gui, width: Float, height: Float, selectedTile: Tile?) {
        nkEditing(gui) {
            window.windowRect?.y(0f)
            window.windowRect?.w(250f)
            window.windowRect?.x(width - (window.windowRect?.w() ?: 0f))
            window.windowRect?.h(height)
            window(width, 0f, 100f, height,
                   0,
                   selectedTile == null) {
                val tile = selectedTile!!
                
                //Render tile to an image
//                tileToImageRenderer.render(tile, worldRenderable)
                val nkImage = NkImage.mallocStack(stack)
                nkImage.handle().id(tileToImageRenderer.texture2D.id)
                dynamicRow(1, 210f) {
                    Nuklear.nk_image(gui.context, nkImage)
                }
                
                font(gui.smallFont) {
                    dynamicRow(2, 20f) {
                        label("Terrain Type:")
                        label(tile.type.toString())
                        
//                        label("Terrain Shape:")
//                        label(tile.shape.toString())
                        
                        label("Major Terrain Feature:")
                        label(tile.majorFeature.toString())
                        
                    }
                    dynamicRow(1, 20f) {
                        label("Minor Terrain Features:")
                        for (minorFeature in tile.minorFeatures) {
                            label(" - $minorFeature")
                        }
                    }
                }
            }
        }
    }
}