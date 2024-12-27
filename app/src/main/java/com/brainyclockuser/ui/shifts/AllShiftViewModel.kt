package com.brainyclockuser.ui.shifts

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.base.BaseViewModel
import com.brainyclockuser.ui.clockin.ShiftsModels
import com.brainyclockuser.utils.AppConstant
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.List
import java.time.ZoneId

@HiltViewModel
class AllShiftViewModel @Inject() constructor() :
    BaseViewModel() {
    val todaysShifts: MutableLiveData<List<ShiftsModels>> = MutableLiveData()
    val nextShifts: MutableLiveData<List<ShiftsModels>> = MutableLiveData()
    val errorMsg: MutableLiveData<String> = MutableLiveData()

    val employeeHistory: MutableLiveData<EmployeeHistoryModel<EmployeeHistoryDataModel>> = MutableLiveData()

    fun callShiftApi() {
        loading.value = true
        apiManager.getShift(
            prefUtils.getIntData(
                BrainyClockUserApp.getAppComponent().provideApplication(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            ).toString()
        ).subscribe({
            loading.value = false
            print(it)
            if (it.success!!) {
                filterShift(it.data)
                callEmployeeHistoryApi()
            } else errorMsg.value = it.message!!
        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun callEmployeeHistoryApi() {
        loading.value = true
        apiManager.getEmployeeHistory(
            prefUtils.getIntData(
                BrainyClockUserApp.getAppComponent().provideApplication(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            ).toString()
        ).subscribe({
            loading.value = false
            print(it)
            if (it.success!!) {
                employeeHistory.value = it
            } else errorMsg.value = it.message!!
        }, {
            loading.value = false
            throwable.value = it
        })
    }

    private fun filterShift(shiftsModel: List<ShiftsModels>?) {
        if (shiftsModel != null) {
//            Log.e("TAG>>>", "filterShift: " + shiftsModel.size)
//            Log.e("TAG>>>", "today : " + getCurrentWeekData(shiftsModel).size)
//            Log.e("TAG>>>", "next : " + getNextWeekData(shiftsModel).size)
            val todayShiftList = getCurrentWeekData(shiftsModel)
            val nextShiftList = getNextWeekData(shiftsModel)
            todaysShifts.value = todayShiftList
            nextShifts.value = nextShiftList
            Log.e("TAG>>>", "filterShift: " + shiftsModel.size)
            Log.e("TAG>>>", "today : " + todayShiftList.size)
            Log.e("TAG>>>", "next : " + nextShiftList.size)
        }
    }

    fun getCurrentWeekData(shifts: List<ShiftsModels>): List<ShiftsModels> {
        val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekEnd = currentWeekStart.plusDays(6)

        return getWeekData(currentWeekStart, currentWeekEnd, shifts)

//        return shifts.filter { shift ->
//
//            // Parse dates only if they're not null
//            val date = shift.date?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing date: $it", e)
//                    null
//                }
//            }
//            val startDate = shift.startDate?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing startDate: $it", e)
//                    null
//                }
//            }
//            val endDate = shift.endDate?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing endDate: $it", e)
//                    null
//                }
//            }
//
//            // updatedDate
//            val updatedDate = shift.updatedDate?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing date: $it", e)
//                    null
//                }
//            }
//
//            // Additional Shift Check when shift is regular
//            val hasAdditionalShift = checkForAdditionalForRegularShift(shifts, shift, currentWeekStart, currentWeekEnd, startDate, endDate, updatedDate)
//
//            val isValidAdditionalShift = checkForValidAdditionalShift(shifts, shift, currentWeekStart, currentWeekEnd, startDate, endDate, updatedDate)
//
//            // Filtering logic based on available date fields
//            when {
//                hasAdditionalShift -> false
//
//                isValidAdditionalShift -> true
//
//                // If only `date` is available, check if it's within the current week
//                date != null && updatedDate == null -> date in currentWeekStart..currentWeekEnd
//
//                // If both `startDate` and `endDate` are available, check for overlap with current week
//                startDate != null && endDate != null && updatedDate == null -> currentWeekStart <= endDate && currentWeekEnd >= startDate
//
//                // If neither `date` nor `startDate` and `endDate` are valid, exclude this shift
//                else -> false
//            }
//        }
    }

    fun getNextWeekData(shifts: List<ShiftsModels>): List<ShiftsModels> {
        val nextWeekStart = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        val nextWeekEnd = nextWeekStart.plusDays(6)
        Log.e("TAG>>>", "Next Start : " + nextWeekStart)
        Log.e("TAG>>>", "Next End : " + nextWeekEnd)

        return getWeekData(nextWeekStart, nextWeekEnd, shifts)

//        return shifts.filter { shift ->
//            // Parse dates only if they're not null
//            val date = shift.date?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing date: $it", e)
//                    null
//                }
//            }
//            val startDate = shift.startDate?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing startDate: $it", e)
//                    null
//                }
//            }
//            val endDate = shift.endDate?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing endDate: $it", e)
//                    null
//                }
//            }
//
//            // updatedDate
//            val updatedDate = shift.updatedDate?.let {
//                try {
//                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
//                } catch (e: Exception) {
//                    Log.e("TAG>>>", "Error parsing date: $it", e)
//                    null
//                }
//            }
//
//            // Additional Shift Check when shift is regular
//            val hasAdditionalShift = checkForAdditionalForRegularShift(shifts, shift, nextWeekStart, nextWeekEnd, startDate, endDate, updatedDate)
//
//            val isValidAdditionalShift = checkForValidAdditionalShift(shifts, shift, nextWeekStart, nextWeekEnd, startDate, endDate, updatedDate)
//
//            // Filtering logic based on available date fields
//            when {
//
//                hasAdditionalShift -> false
//
//                isValidAdditionalShift -> true
//
//                // If only `date` is available, check if it's within the next week
//                date != null && updatedDate == null -> date in nextWeekStart..nextWeekEnd
//
//                // If both `startDate` and `endDate` are available, check for overlap with next week
//                startDate != null && endDate != null && updatedDate == null -> nextWeekStart <= endDate && nextWeekEnd >= startDate
//
//                // If neither `date` nor `startDate` and `endDate` are valid, exclude this shift
//                else -> false
//            }
//        }
    }

    fun getWeekData(weekStart: LocalDate,weekEnd: LocalDate, shifts: List<ShiftsModels>): List<ShiftsModels> {
        var result = ArrayList<ShiftsModels>();
        for(currentDay in generateSequence(weekStart) { it.plusDays(1) }.takeWhile { !it.isAfter(weekEnd) }) {
            shifts.forEach { shift ->
                if(checkShiftIsAvailableDate(currentDay, shift)) {
                    if(!result.contains(shift)) {
                        result.add(shift)
                    }
                    if(isAdditionalShift(shift)) {
//                        var additionalShift = checkForAdditionalForRegularShift(shifts, shift, currentDay)
//                        if(additionalShift != null) {
//
//                        }
                        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
                        val dayName = dayFormatter.format(Date.from(currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant())).lowercase()
//
                        var regularShift = findRegularShiftFromAdditional(shifts, shift)
                        if(result.contains(regularShift)) {
                            val index = result.indexOf(regularShift)
                            val newValue = result.get(index)
                            val clonedShift = deepClone(newValue, ShiftsModels::class.java)
                            clonedShift.days = clonedShift.days!!.map { day ->
                                if(day.day == dayName) {
                                    day.timeIn = null
                                    day.timeOut = null
                                }
                                day
                            }
                            result[index] = clonedShift
                        }
                    }

                }
            }
        }
        return  result;
    }

    /*fun getCurrentWeekData(shifts: List<ShiftsModels>): List<ShiftsModels> {
        val currentWeekStart =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentWeekEnd = currentWeekStart.plusDays(6)

        return shifts.filter { shift ->

            Log.e("TAG>>>", "0 this date : " + shift.date)
            Log.e("TAG>>>", "0 this startDate : " + shift.startDate)
            Log.e("TAG>>>", "0 this endDate : " + shift.endDate)

            val date = shift.date?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing date: $it", e)
                    null
                }
            }

            val startDate = shift.startDate?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing startDate: $it", e)
                    null
                }
            }

            val endDate = shift.endDate?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing endDate: $it", e)
                    null
                }
            }

            when {
                // If startDate or endDate is null, use Date to check if it's in the current week
                startDate == null || endDate == null -> date in currentWeekStart..currentWeekEnd

                // Otherwise, check if current week overlaps with the startDate and endDate range
                else -> currentWeekStart <= endDate && currentWeekEnd >= startDate
            }
        }
    }

    fun getNextWeekData(shifts: List<ShiftsModels>): List<ShiftsModels> {
        val nextWeekStart = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        val nextWeekEnd = nextWeekStart.plusDays(6)

        return shifts.filter { shift ->

            Log.e("TAG>>>", "0 next date : " + shift.date)
            Log.e("TAG>>>", "0 next startDate : " + shift.startDate)
            Log.e("TAG>>>", "0 next endDate : " + shift.endDate)

            val date = shift.date?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing date: $it", e)
                    null
                }
            }

            val startDate = shift.startDate?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing startDate: $it", e)
                    null
                }
            }

            val endDate = shift.endDate?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing endDate: $it", e)
                    null
                }
            }

            when {
                // If startDate or endDate is null, use Date to check if it's in the next week
                startDate == null || endDate == null -> date in nextWeekStart..nextWeekEnd

                // Otherwise, check if next week overlaps with the startDate and endDate range
                else -> nextWeekStart <= endDate && nextWeekEnd >= startDate
            }
        }
    }*/
    
}

fun checkShiftIsAvailableDate(currentDate: LocalDate, shift: ShiftsModels): Boolean {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    // Parse dates only if they're not null
    val date = shift.date?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing date: $it", e)
            null
        }
    }
    val startDate = shift.startDate?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing startDate: $it", e)
            null
        }
    }
    val endDate = shift.endDate?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing endDate: $it", e)
            null
        }
    }

    // updatedDate
    val updatedDate = shift.updatedDate?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing date: $it", e)
            null
        }
    }

    return when {
        startDate != null && endDate != null && updatedDate != null -> updatedDate.equals(currentDate)
        startDate != null && endDate != null -> currentDate in startDate..endDate
        date != null -> date.equals(currentDate)
        else -> false
    }

}

fun checkForAdditionalForRegularShift(shifts: List<ShiftsModels>, shift: ShiftsModels, currentDate: LocalDate): ShiftsModels? {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val startDate = shift.startDate?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing startDate: $it", e)
            null
        }
    }
    val endDate = shift.endDate?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing endDate: $it", e)
            null
        }
    }

    // updatedDate
    val updatedDate = shift.updatedDate?.let {
        try {
            LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("TAG>>>", "Error parsing date: $it", e)
            null
        }
    }
    val isRegularShift = startDate != null && endDate != null && updatedDate == null
    if(isRegularShift) {
        var additionalShift = shifts.find { it.startDate == shift.startDate && it.endDate == shift.endDate && it.updatedDate != null && it.updatedDate != "" }
        if(additionalShift != null) {
            var additionalShiftUpdateDate = additionalShift?.updatedDate?.let {
                try {
                    LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
                } catch (e: Exception) {
                    Log.e("TAG>>>", "Error parsing date: $it", e)
                    null
                }
            }
            if(additionalShiftUpdateDate != null && additionalShiftUpdateDate.equals(currentDate)) {
                return additionalShift
            }
        }
    }
    return null
}

fun findRegularShiftFromAdditional(shifts: List<ShiftsModels>, additionalShift: ShiftsModels): ShiftsModels? {
    return shifts.find { it.startDate == additionalShift.startDate && it.endDate == additionalShift.endDate && (it.updatedDate == null || it.updatedDate == "") }
}

fun isAdditionalShift(shift: ShiftsModels): Boolean {
    val status = (shift.startDate != null && shift.endDate != null)
    return status && shift.updatedDate != null
}

fun checkForAdditionalForRegularShift(shifts: List<ShiftsModels>, shift: ShiftsModels, currentWeekStart: LocalDate, currentWeekEnd: LocalDate, startDate: LocalDate?, endDate: LocalDate?, updatedDate: LocalDate?): Boolean {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var additionalShift: ShiftsModels? = null
    val isRegularShift = startDate != null && endDate != null && updatedDate == null
    if(isRegularShift) {
        additionalShift = shifts.find { it.startDate == shift.startDate && it.endDate == shift.endDate && it.updatedDate != null && it.updatedDate != "" }
    } else {
        return false
    }
    var additionalShiftUpdateDate: LocalDate? = null
    if(additionalShift != null) {
        additionalShiftUpdateDate = additionalShift.updatedDate?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
            } catch (e: Exception) {
                Log.e("TAG>>>", "Error parsing date: $it", e)
                null
            }
        }
    }
    return additionalShift != null && additionalShiftUpdateDate != null && (additionalShiftUpdateDate in currentWeekStart..currentWeekEnd)
}

fun checkForValidAdditionalShift(shifts: List<ShiftsModels>, shift: ShiftsModels, currentWeekStart: LocalDate, currentWeekEnd: LocalDate, startDate: LocalDate?, endDate: LocalDate?, updatedDate: LocalDate?): Boolean {
//        var regularShift: ShiftsModels? = null
    val isAdditionalShift = startDate != null && endDate != null && updatedDate != null
    if(isAdditionalShift && updatedDate != null) {
//            regularShift = shifts.find { it.startDate == shift.startDate && it.endDate == shift.endDate && (it.updatedDate == null || it.updatedDate == "") }
        return (updatedDate in currentWeekStart..currentWeekEnd)
    } else {
        return false
    }
}

fun checkForAdditionalForRegularShiftDay(shifts: List<ShiftsModels>, shift: ShiftsModels, currentWeekStart: LocalDate, currentWeekEnd: LocalDate, startDate: LocalDate?, endDate: LocalDate?, updatedDate: LocalDate?): Boolean {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var additionalShift: ShiftsModels? = null
    val isRegularShift = startDate != null && endDate != null && updatedDate == null
    val updatedDateFormatted = LocalDateTime.now().toLocalDate().atStartOfDay().format(dateTimeFormatter)
    if(isRegularShift) {
        additionalShift = shifts.find { it.startDate == shift.startDate && it.endDate == shift.endDate && it.updatedDate == updatedDateFormatted }
    } else {
        return false
    }
    var additionalShiftUpdateDate: LocalDate? = null
    if(additionalShift != null) {
        additionalShiftUpdateDate = additionalShift.updatedDate?.let {
            try {
                LocalDateTime.parse(it, dateTimeFormatter).toLocalDate()
            } catch (e: Exception) {
                Log.e("TAG>>>", "Error parsing date: $it", e)
                null
            }
        }
    }
    return additionalShift != null && additionalShiftUpdateDate != null && (additionalShiftUpdateDate in currentWeekStart..currentWeekEnd)
}

fun <T> deepClone(obj: T, clazz: Class<T>): T {
    val gson = Gson()
    val json = gson.toJson(obj) // Convert to JSON
    return gson.fromJson(json, clazz) // Deserialize back to a new object
}