
package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.DeniedAlwaysException
import kz.qbox.qbox_permissions.Permission
import kz.qbox.qbox_permissions.PermissionState
import kz.qbox.qbox_permissions.mainContinuation
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AVCapturePermissionDelegate(
    private val type: AVMediaType,
    private val permission: Permission
) : PermissionDelegate {
    override suspend fun providePermission() {
        val status: AVAuthorizationStatus = currentAuthorizationStatus()
        when (status) {
            AVAuthorizationStatusAuthorized -> return
            AVAuthorizationStatusNotDetermined -> {
                val isGranted: Boolean = suspendCoroutine { continuation ->
                    AVCaptureDevice.requestAccess(type) { continuation.resume(it) }
                }
                if (isGranted) return
                else throw DeniedAlwaysException(permission)
            }
            AVAuthorizationStatusDenied -> throw DeniedAlwaysException(permission)
            else -> throw IllegalStateException("unknown authorization status $status")
        }
    }

    override fun isPermissionGranted(): Boolean {
        return currentAuthorizationStatus() == AVAuthorizationStatusAuthorized
    }

    override suspend fun getPermissionState(): PermissionState {
        val status = currentAuthorizationStatus()
        return when (status) {
            AVAuthorizationStatusAuthorized -> PermissionState.Granted
            AVAuthorizationStatusNotDetermined -> PermissionState.NotDetermined
            AVAuthorizationStatusDenied -> PermissionState.DeniedAlways
            else -> throw IllegalStateException("unknown authorization status $status")
        }
    }

    private fun currentAuthorizationStatus(): AVAuthorizationStatus {
        return AVCaptureDevice.authorizationStatusForMediaType(type)
    }
}

private fun AVCaptureDevice.Companion.requestAccess(
    type: AVMediaType,
    callback: (isGranted: Boolean) -> Unit
) {
    this.requestAccessForMediaType(type, mainContinuation { isGranted: Boolean ->
        callback(isGranted)
    })
}
