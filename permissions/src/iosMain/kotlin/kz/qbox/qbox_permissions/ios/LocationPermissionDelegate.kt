package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.DeniedAlwaysException
import kz.qbox.qbox_permissions.LocationManagerDelegate
import kz.qbox.qbox_permissions.Permission
import kz.qbox.qbox_permissions.PermissionState
import platform.CoreLocation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class LocationPermissionDelegate(
    private val locationManagerDelegate: LocationManagerDelegate,
    private val permission: Permission
) : PermissionDelegate {
    override suspend fun providePermission() {
        return provideLocationPermission(CLLocationManager.authorizationStatus())
    }

    override fun isPermissionGranted(): Boolean {
        val status: CLAuthorizationStatus = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedAlways
                || status == kCLAuthorizationStatusAuthorizedWhenInUse
    }

    override suspend fun getPermissionState(): PermissionState {
        val status: CLAuthorizationStatus = CLLocationManager.authorizationStatus()
        return when (status) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionState.Granted
            kCLAuthorizationStatusNotDetermined -> PermissionState.NotDetermined
            kCLAuthorizationStatusDenied -> PermissionState.DeniedAlways
            else -> throw IllegalStateException("unknown location authorization status $status")
        }
    }

    private suspend fun provideLocationPermission(
        status: CLAuthorizationStatus
    ) {
        when (status) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> return
            kCLAuthorizationStatusNotDetermined -> {
                val newStatus = suspendCoroutine<CLAuthorizationStatus> { continuation ->
                    locationManagerDelegate.requestLocationAccess { continuation.resume(it) }
                }
                provideLocationPermission(newStatus)
            }
            kCLAuthorizationStatusDenied -> throw DeniedAlwaysException(permission)
            else -> throw IllegalStateException("unknown location authorization status $status")
        }
    }
}
