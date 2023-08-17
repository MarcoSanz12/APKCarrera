package com.gf.common.extensions

import android.Manifest
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment

fun Fragment.isGranted(permission: AppPermission) = run {
    context?.let {
        (PermissionChecker.checkSelfPermission(it, permission.permissionName
        ) == PermissionChecker.PERMISSION_GRANTED)
    } ?: false
}

fun Fragment.shouldShowRationale(permission: AppPermission) = run {
    shouldShowRequestPermissionRationale(permission.permissionName)
}

fun Fragment.requestPermission(permissions: Array<out String>, requestCode: Int ) {
    requestPermissions(permissions, requestCode)
}

//private fun Fragment.goToAppDetailsSettings() {
//    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//        data = Uri.fromParts("package", context?.packageName, null)
//    }
//    activity?.let {
//        it.startActivityForResult(intent, 0)
//    }
//}


private fun mapPermissionsAndResults(
    permissions: Array<out String>, grantResults: IntArray
): Map<String, Int> = permissions.mapIndexedTo(mutableListOf()
) { index, permission -> permission to grantResults[index] }.toMap()


sealed class AppPermission(
    val permissionName: String, val requestCode: Int
) {
    companion object {
        val permissions: List<AppPermission> by lazy {
            listOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION,
                ACCESS_BACKGROUND_LOCATION,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE
            )
        }
    }


    object ACCESS_FINE_LOCATION : AppPermission(Manifest.permission.ACCESS_FINE_LOCATION, 42
    )

    object ACCESS_COARSE_LOCATION : AppPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 43
    )
    object ACCESS_BACKGROUND_LOCATION : AppPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, 46
    )
    object READ_EXTERNAL_STORAGE : AppPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 44
    )

    object WRITE_EXTERNAL_STORAGE : AppPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 45
    )

}