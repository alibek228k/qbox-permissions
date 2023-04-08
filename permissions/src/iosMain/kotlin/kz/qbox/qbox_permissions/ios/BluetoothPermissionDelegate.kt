package kz.qbox.qbox_permissions.ios

import kz.qbox.qbox_permissions.DeniedAlwaysException
import kz.qbox.qbox_permissions.DeniedException
import kz.qbox.qbox_permissions.Permission
import kz.qbox.qbox_permissions.PermissionState
import platform.CoreBluetooth.*
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class BluetoothPermissionDelegate(
    private val permission: Permission
) : PermissionDelegate {
    override suspend fun providePermission() {
        // To maintain compatibility with iOS 12 (@see https://developer.apple.com/documentation/corebluetooth/cbmanagerauthorization)
        val isNotDetermined: Boolean =
            if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
                CBManager.authorization == CBManagerAuthorizationNotDetermined
            } else {
                CBCentralManager().state == CBManagerStateUnknown
            }

        val state: CBManagerState = if (isNotDetermined) {
            suspendCoroutine { continuation ->
                CBCentralManager(object : NSObject(), CBCentralManagerDelegateProtocol {
                    override fun centralManagerDidUpdateState(central: CBCentralManager) {
                        continuation.resume(central.state)
                    }
                }, null)
            }
        } else {
            CBCentralManager().state
        }

        when (state) {
            CBManagerStatePoweredOn -> return
            CBManagerStateUnauthorized -> throw DeniedAlwaysException(permission)
            CBManagerStatePoweredOff ->
                throw DeniedException(permission, "Bluetooth is powered off")
            CBManagerStateResetting ->
                throw DeniedException(permission, "Bluetooth is restarting")
            CBManagerStateUnsupported ->
                throw DeniedAlwaysException(permission, "Bluetooth is not supported on this device")
            CBManagerStateUnknown ->
                throw IllegalStateException("Bluetooth state should be known at this point")
            else ->
                throw IllegalStateException("Unknown state (Permissions library should be updated) : $state")
        }
    }

    override fun isPermissionGranted(): Boolean {
        // To maintain compatibility with iOS 12 (@see https://developer.apple.com/documentation/corebluetooth/cbmanagerauthorization)
        if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
            return CBManager.authorization == CBManagerAuthorizationAllowedAlways
        }
        return CBCentralManager().state == CBManagerStatePoweredOn
    }

    override suspend fun getPermissionState(): PermissionState {
        // To maintain compatibility with iOS 12 (@see https://developer.apple.com/documentation/corebluetooth/cbmanagerauthorization)
        if (CBManager.resolveClassMethod(NSSelectorFromString("authorization"))) {
            val state: CBManagerAuthorization = CBManager.authorization
            return when (state) {
                CBManagerAuthorizationNotDetermined -> PermissionState.NotDetermined
                CBManagerAuthorizationAllowedAlways, CBManagerAuthorizationRestricted -> PermissionState.Granted
                CBManagerAuthorizationDenied -> PermissionState.DeniedAlways
                else -> throw IllegalStateException("unknown state $state")
            }
        }
        val state: CBManagerState = CBCentralManager().state
        return when (state) {
            CBManagerStatePoweredOn -> PermissionState.Granted
            CBManagerStateUnauthorized, CBManagerStatePoweredOff,
            CBManagerStateResetting, CBManagerStateUnsupported -> PermissionState.DeniedAlways
            CBManagerStateUnknown -> PermissionState.NotDetermined
            else -> throw IllegalStateException("unknown state $state")
        }
    }
}
