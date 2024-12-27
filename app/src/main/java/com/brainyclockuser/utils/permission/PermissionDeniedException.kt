package com.brainyclockuser.utils.permission

/**
 * Created  on 6/7/2017.
 * custom permission to throw when user denied for permission
 * useful while developing libraries
 */
class PermissionDeniedException(
    message: String?,
    /**
     * will be one constants defined in this class
     *
     * @return constant representing group of permission for single functional requirement
     */
    val deniedPermission: Int
) : Exception(message) {

    companion object {
        const val CAMERA = 1
        const val GALLERY = 2
        const val LOCATION = 3
        const val CONTACTS = 4
        const val STORAGE = 5
        const val CALL = 6
    }
}