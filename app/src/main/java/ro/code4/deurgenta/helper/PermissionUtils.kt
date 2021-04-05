package ro.code4.deurgenta.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class PermissionUtils(val activity: Activity) {

    private var resultListener: ResultListener? = null

    interface ResultListener {
        fun permissionsGranted()
        fun permissionsDenied()
    }

    fun request(resultListener: ResultListener) {
        this.resultListener = resultListener
        val missingPermissions = getPermissionRequest()
        if (missingPermissions.isEmpty()) {
            resultListener.permissionsGranted()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // Exclude CHANGE_NETWORK_STATE as it does not require explicit user approval.
    // This workaround is needed for devices running Android 6.0.0,
    // see https://issuetracker.google.com/issues/37067994
    private fun getPermissionRequest(): Array<String> {
        val permissionList = ArrayList<String>()
        try {
            val packageInfo = activity.packageManager.getPackageInfo(
                activity.packageName, PackageManager.GET_PERMISSIONS
            )
            if (packageInfo.requestedPermissions != null) {
                for (permission in packageInfo.requestedPermissions) {
                    if (ContextCompat.checkSelfPermission(
                            activity, permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && permission == Manifest.permission.CHANGE_NETWORK_STATE) {
                            // Exclude CHANGE_NETWORK_STATE as it does not require explicit user approval.
                            // This workaround is needed for devices running Android 6.0.0,
                            // see https://issuetracker.google.com/issues/37067994
                            continue
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                            (permission == Manifest.permission.ACTIVITY_RECOGNITION || permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        ) {
                            continue
                        }
                        permissionList.add(permission)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return permissionList.toTypedArray()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (resultListener == null) {
            return
        }
        if (grantResults.size == 0) {
            // Request was cancelled.
            return
        }
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var allGranted = true
            for (result in grantResults) {
                allGranted = allGranted and (result == PackageManager.PERMISSION_GRANTED)
            }
            if (allGranted) {
                resultListener!!.permissionsGranted()
            } else {
                resultListener!!.permissionsDenied()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 42
    }

}