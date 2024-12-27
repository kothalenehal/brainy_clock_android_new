package com.brainyclockuser.utils

object ApiConstants {
//    var BASE_URL = "https://fbz8oajqj5.execute-api.us-east-1.amazonaws.com/api/"
//    var BASE_URL = "https://5g9kqnv2aa.execute-api.us-east-1.amazonaws.com/api/"
    var BASE_URL = "https://8m02o9way6.execute-api.us-east-1.amazonaws.com/prod/api/"
//    var BASE_URL = "https://5g9kqnv2aa.execute-api.us-east-1.amazonaws.com/api/"
//    var BASE_URL = "http://54.210.1.150:3000/api/"

    /**
     * api endpoint listed hereFMeanwhile try this login
     */
    interface EndPoints {
        companion object {
            const val SIGNUP = "auth/signup"
            const val LOGIN = "auth/login"
            const val LOGOUT = "auth/logout"
            const val CHANGE_PASSWORD = "auth/change-password"
            const val FORGOT_PASSWORD = "auth/forgot-password"
            const val VERIFY_FORGOT_PASSWORD = "auth/verify-forgot-password"
            const val REFRESH_TOKEN = "auth/refresh-token"

            const val GET_EMPLOYEE_SHIFTS = "employee/get-employee-shifts"
            const val ATTENDANCE_MARK = "attendance/mark"
            const val REQUEST_OVERTIME = "employee/request-overtime"
            const val VERIFYQR_CODE = "location/verify-location?"
            const val GET_SITE_EMPLOYEES = "getSiteEmployee"
            const val MARK_SITE_EMPLOYEE_ATTENDANCE = "createAttendance"
            const val GET_EMPLOYEE_HISTORY = "getEmployeeHistory"
        }
    }

    /**
     * api parameter listed here
     */
    interface params {
        companion object {
            const val AUTHORIZATION = "Authorization"
            const val EMAIL = "email"
            const val PASSWORD = "password"
            const val FIRSTNAME = "firstName"
            const val LASTNAME = "lastName"
            const val EMPLOYEE_ID = "employeeId"
            const val COMPANY_ID = "companyId"
        }
    }

    interface ResponseCode {
        companion object {
            const val RESPONSE_SUCCESS = 1
            const val RESPONSE_FAIL = 0
            const val NOT_FOUND = 404
            const val CONFLICT = 400
            const val HTTP_SUCCESS = 200
            const val HTTP_INTERNAL_SERVER_ERROR = 500
            const val ACCESS_TOKEN_EXPIRE = 104
            const val HTTP_REQUEST_TIMEOUT = 408
        }
    }
}