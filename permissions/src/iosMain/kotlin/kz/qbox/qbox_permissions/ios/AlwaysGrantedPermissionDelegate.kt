package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.PermissionState

internal class AlwaysGrantedPermissionDelegate : PermissionDelegate {
    override suspend fun providePermission() = Unit

    override fun isPermissionGranted(): Boolean = true

    override suspend fun getPermissionState(): PermissionState = PermissionState.Granted
}
