
package kz.qbox.qbox_permissions

import kz.qbox.qbox_permissions.Permission

class RequestCanceledException(
    val permission: Permission,
    message: String? = null
) : Exception(message)
