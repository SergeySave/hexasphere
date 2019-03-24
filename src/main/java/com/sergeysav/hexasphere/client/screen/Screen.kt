package com.sergeysav.hexasphere.client.screen

import com.sergeysav.hexasphere.Hexasphere

/**
 * @author sergeys
 *
 * @constructor Creates a new Screen
 */
interface Screen {
    //
    //    /**
    //     * Create anything needed for this screen's operation
    //     * This will only ever be called once
    //     * This will be called right after the constructor
    //     */
    //    fun create()
    
    /**
     * Register any needed event handlers to the given application
     * Will not be called if already registered (i.e. the screen has not been unregistered yet)
     */
    fun register(application: Hexasphere)
    
    /**
     * Called to render the screen
     * @param delta the amount of time in seconds since the last time this was called
     */
    fun render(delta: Double)
    
    /**
     * Unregister any needed event handlers from the given application
     * Always called after a register
     */
    fun unregister(application: Hexasphere)
    
    /**
     * Delete anything no longer needed
     * This will only ever be called once
     */
    fun cleanup()
}