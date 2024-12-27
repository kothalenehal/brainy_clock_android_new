package com.brainyclockuser.ui.clockin

data class Employee(
    val name: String,
    var status: Int
) {
    companion object {
        const val STATUS_PRESENT = 0
        const val STATUS_LATE = 1
        const val STATUS_ABSENT = 2
        const val STATUS_CLOCKED_OUT = 3
    }
}