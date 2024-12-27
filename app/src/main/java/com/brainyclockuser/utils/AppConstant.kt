package com.brainyclockuser.utils

object AppConstant {
    const val IS_DEBUGGABLE = true
    var IsConnect = false

    const val UPCOMING_SHIFT = 0
    const val ACTIVE_SHIFT = 1
    const val CLOCKED_OUT_SHIFT = 2
    const val ACTIVE_OVERTIME_SHIFT = 3
    const val ACTIVE_IN_OVERTIME_SHIFT = 4
    const val ACTIVE_OVERTIME_NA_SHIFT = 5
    const val ACTIVE_OVERTIME_PENDING_SHIFT = 6
    const val OTHER_SHIFT = 7

    const val CLOCK_IN = 1
    const val CLOCK_OUT = 2
    const val LUNCH_IN = 3
    const val LUNCH_OUT = 4

    const val BEGIN_SHIFT_BEFORE = -30

    const val EMPLOYEE_LIST_USER_TYPE = 5



    interface SharedPreferences {
        companion object {
            const val ACCESS_TOKEN = "accessToken"
            const val REFRESH_TOKEN = "refreshToken"
            const val TOKEN_EXPIRE_ON = "tokenExpireOn"
            const val EMAIL = "email"
            const val NAME = "name"
            const val DEPARTMENTNAME = "departmentName"
            const val EMPLOYEE_ID = "employeeId"
            const val SHIFT_ID = "shiftId"
            const val EMP_ID = "empID"
            const val COMPANY_ID = "compId"
            const val ApplicationType = "appType"
            const val Latitude = "lat"
            const val Longitude = "long"
            const val GeofenceRadious = "geofence_radius"
            const val OfficeLocationName = "location_name"

            //smell test
            const val SELECTED_MENU = "SelectedMenu"
            const val SELECTED_KIT_SIZE = "SelectedKitSize"
            const val PREF_SCANNED_RESULT = "PrefScanResult"
            const val PREF_SCANNED_RESULT_WITH_TIMER = "PrefScanResultWithTimer"
            const val PREF_SELECTED_ANSWERS = "PrefSelectedAnswers"
            const val CORRECT_ANSWER_COUNT = "correctAnswerCount"
            const val WITH_TIMER = "withTimer"
            const val ODOR_DIFF_QUESTION = "odorDiffQuestion"
            const val ODOR_INTENSITY_QUESTION = "odorIntensityQuestion"
            const val START_TIME = "startTime"
            const val END_TIME = "endTime"


            //trace aware
            const val GENERATED_RANDOM_QUESTION_NUMBERS =
                "generatedRandomNumbers"//questions have been asked
            const val GENERATED_RANDOM_IMAGES_INDEX = "generatedRandomImages"
            const val CURRENT_QUESTION = "currentQuestion"
            const val COMPLETED_TRACES = "completedTraces"
            const val SHOW_RECALL_INTRO_MSG = "showRecallIntroMsg"

        }
    }

    interface DateTime {
        companion object {
            const val DD_MM_YYYY = "dd/MM/yyyy"
            const val MMM_YYYY = "MMM yyyy"
        }
    }

    interface BundleExtra {
        companion object {
            const val SCANNED_RESULT = "ScanResult"
            const val RETRAINING = "Retraining"
            const val TRACES = "traces"
        }
    }

    interface Delays {
        companion object {
            const val MIN_TIME_BETWEEN_CLICKS: Long = 200

        }
    }
}