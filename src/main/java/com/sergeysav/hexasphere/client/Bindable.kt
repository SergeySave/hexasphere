package com.sergeysav.hexasphere.client

/**
 * @author sergeys
 */
interface Bindable {
    fun bind()
    fun unbind()
}

fun Bindable.bound(inner: () -> Unit) {
    try {
        bind()
        inner()
    } finally {
        unbind()
    }
}
