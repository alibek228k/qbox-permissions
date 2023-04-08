package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.Permission
import kz.qbox.qbox_permissions.PermissionState

interface PermissionsControllerProtocol {
    suspend fun providePermission(permission: Permission)
    fun isPermissionGranted(permission: Permission): Boolean
    suspend fun getPermissionState(permission: Permission): PermissionState
    fun openAppSettings()
}
