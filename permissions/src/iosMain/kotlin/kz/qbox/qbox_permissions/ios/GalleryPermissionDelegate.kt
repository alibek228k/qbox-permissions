package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.DeniedAlwaysException
import kz.qbox.qbox_permissions.Permission
import kz.qbox.qbox_permissions.PermissionState
import kz.qbox.qbox_permissions.mainContinuation
import platform.Photos.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class GalleryPermissionDelegate : PermissionDelegate {
    override suspend fun providePermission() {
        providePermission(PHPhotoLibrary.authorizationStatus())
    }

    private suspend fun providePermission(status: PHAuthorizationStatus) {
        return when (status) {
            PHAuthorizationStatusAuthorized -> return
            PHAuthorizationStatusNotDetermined -> {
                val newStatus = suspendCoroutine<PHAuthorizationStatus> { continuation ->
                    requestGalleryAccess { continuation.resume(it) }
                }
                providePermission(newStatus)
            }
            PHAuthorizationStatusDenied -> throw DeniedAlwaysException(Permission.GALLERY)
            else -> throw IllegalStateException("unknown gallery authorization status $status")
        }
    }

    override fun isPermissionGranted(): Boolean {
        return PHPhotoLibrary.authorizationStatus() == PHAuthorizationStatusAuthorized
    }

    override suspend fun getPermissionState(): PermissionState {
        val status: PHAuthorizationStatus = PHPhotoLibrary.authorizationStatus()
        return when (status) {
            PHAuthorizationStatusAuthorized -> PermissionState.Granted
            PHAuthorizationStatusNotDetermined -> PermissionState.NotDetermined
            PHAuthorizationStatusDenied -> PermissionState.DeniedAlways
            else -> throw IllegalStateException("unknown gallery authorization status $status")
        }
    }
}

private fun requestGalleryAccess(callback: (PHAuthorizationStatus) -> Unit) {
    PHPhotoLibrary.requestAuthorization(mainContinuation { status: PHAuthorizationStatus ->
        callback(status)
    })
}
