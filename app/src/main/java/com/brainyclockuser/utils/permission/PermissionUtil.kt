package com.brainyclockuser.utils.permission

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by baurine on 2/10/17.
 */
@Singleton
class PermissionUtil @Inject constructor() {
    fun checkMissingPermission(
        fragment: Fragment,
        permissionCode: Int,
        vararg permissions: String
    ): Boolean {
        val revokeedPermission: MutableList<String> = ArrayList()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (fragment.activity?.let {
                        ContextCompat.checkSelfPermission(
                            it,
                            permission
                        )
                    } == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("---", "Permission $permission is granted")
                } else {
                    Log.i("---", "Permission $permission is revoked")
                    revokeedPermission.add(permission)
                }
            }
            if (revokeedPermission.isEmpty()) {
                true
            } else {
                fragment.requestPermissions(revokeedPermission.toTypedArray(), permissionCode)
                false
            }
        } else true
    }

    fun checkMissingPermission(
        activity: AppCompatActivity,
        permissionCode: Int,
        vararg permissions: String
    ): Boolean {
        val revokedPermissions: MutableList<String> = ArrayList()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //iterating over permission, if not granted then add to our list and ask permission to user
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("---", "Permission $permission is granted")
                } else {
                    Log.i("---", "Permission $permission is revoked")
                    revokedPermissions.add(permission) //to ask only non-granted permissions
                }
            }
            //if all permissions are granted, list of revoked permissions will be empty
            if (revokedPermissions.isEmpty()) {
                true
            } else {
                activity.requestPermissions(revokedPermissions.toTypedArray(), permissionCode)
                false
            }
        } else {
            true
        }
    }

    fun checkGrantResults(grantResults: IntArray): Boolean {
        var isGranted = true
        for (grantResult in grantResults) { //iterate though grant results for asked permissions
            if (grantResult != PackageManager.PERMISSION_GRANTED) { //if any asked permission is not granted
                isGranted = false
                break
            }
        }
        Log.i("---", "checkGrantResults() returned: $isGranted")
        return isGranted
    }
}