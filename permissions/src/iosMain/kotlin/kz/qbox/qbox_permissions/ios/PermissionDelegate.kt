
package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.PermissionState

internal interface PermissionDelegate {
    suspend fun providePermission()
    fun isPermissionGranted(): Boolean
    suspend fun getPermissionState(): PermissionState
}
