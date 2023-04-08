package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.LocationManagerDelegate
import kz.qbox.qbox_permissions.Permission
import kz.qbox.qbox_permissions.PermissionState
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class PermissionsController : PermissionsControllerProtocol {
    private val locationManagerDelegate = LocationManagerDelegate()

    override suspend fun providePermission(permission: Permission) {
        return getDelegate(permission).providePermission()
    }

    override fun isPermissionGranted(permission: Permission): Boolean {
        return getDelegate(permission).isPermissionGranted()
    }

    override suspend fun getPermissionState(permission: Permission): PermissionState {
        return getDelegate(permission).getPermissionState()
    }

    override fun openAppSettings() {
        val settingsUrl: NSURL = NSURL.URLWithString(UIApplicationOpenSettingsURLString)!!
        UIApplication.sharedApplication.openURL(settingsUrl)
    }

    private fun getDelegate(permission: Permission): PermissionDelegate {
        return when (permission) {
            Permission.REMOTE_NOTIFICATION -> RemoteNotificationPermissionDelegate()
            Permission.CAMERA -> AVCapturePermissionDelegate(AVMediaTypeVideo, permission)
            Permission.GALLERY -> GalleryPermissionDelegate()
            Permission.STORAGE, Permission.WRITE_STORAGE -> AlwaysGrantedPermissionDelegate()
            Permission.LOCATION, Permission.COARSE_LOCATION ->
                LocationPermissionDelegate(locationManagerDelegate, permission)
            Permission.RECORD_AUDIO -> AVCapturePermissionDelegate(AVMediaTypeAudio, permission)
            Permission.BLUETOOTH_LE, Permission.BLUETOOTH_SCAN,
            Permission.BLUETOOTH_ADVERTISE, Permission.BLUETOOTH_CONNECT ->
                BluetoothPermissionDelegate(permission)
        }
    }
}
