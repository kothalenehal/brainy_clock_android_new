package com.brainyclockuser.ui.shifts

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.Transformation
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainyclockuser.R
import com.brainyclockuser.databinding.RowShiftParentBinding
import com.brainyclockuser.ui.clockin.ShiftsModels
import com.brainyclockuser.utils.OnItemClickListener
import java.util.*

class ShiftsAdapter(
    private val list: List<ShiftsModels>,
    private val isTodaysShift: Boolean,
    private val onClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<ShiftsAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var animation: RotateAnimation

    class ViewHolder(val binding: RowShiftParentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = RowShiftParentBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.isTodaysShift = isTodaysShift
        holder.binding.shiftName = list[position].shiftName

        val shortDayNames = mapOf(
            "sun" to "Sun",
            "mon" to "Mon",
            "tue" to "Tue",
            "wed" to "Wed",
            "thu" to "Thu",
            "fri" to "Fri",
            "sat" to "Sat"
        )
        val days = list[position].days  // days is a List<ShiftDay>, not a string
        var allDays = ""
        val today =
            Calendar.getInstance()[Calendar.DAY_OF_WEEK]  // Get today's day as an integer (1 = Sunday, 7 = Saturday)

        for (shiftDay in days
            ?: emptyList()) {  // Safe call with emptyList as fallback in case days is null
            val shortDayName =
                shortDayNames[shiftDay.day] ?: shiftDay.day  // Convert full day name to short name
            // Compare the current day's name (from `today`) with the `shiftDay.day` to determine if it's today
            if (shiftDay.day == shortDayNames.keys.toList()[today - 1]) {  // `today - 1` because Calendar.DAY_OF_WEEK starts at 1 (Sunday)
                allDays += " <b>$shortDayName</b>"  // Bold the current day
            } else {
                allDays += " $shortDayName"  // Regular day
            }
        }

        holder.binding.tvShiftDays.text =
            HtmlCompat.fromHtml(allDays.trim(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.binding.rvDates.layoutManager = LinearLayoutManager(context)

        holder.binding.tvShiftDays.visibility = View.VISIBLE
        // Assuming holder.binding.clBG is a View (e.g., ConstraintLayout)
        val layoutParams = holder.binding.clBG.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = 16 // Set the start margin to 10 pixels
        holder.binding.clBG.layoutParams = layoutParams // Apply the updated layout parameters
        holder.binding.cardShift.setCardBackgroundColor(
            ContextCompat.getColor(context, R.color.Orange)
        )

        holder.binding.ivArrow.setOnClickListener {
            animate(holder)
        }

        holder.binding.cardShift.setOnClickListener { onClickListener.onItemClick(holder.absoluteAdapterPosition) }
        holder.binding.rvDates.adapter = ShiftsDaysAdapter(list[position].days!!, isTodaysShift)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun animate(holder: ViewHolder) {
        if (holder.binding.llDetails.visibility == View.GONE) {
            animation = RotateAnimation(
                180f,
                0f,
                RotateAnimation.RELATIVE_TO_SELF,
                0.5f,
                RotateAnimation.RELATIVE_TO_SELF,
                0.5f
            )

            holder.binding.tvShiftDays.visibility = View.GONE
            holder.binding.cardShift.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.white)
            )
            // Assuming holder.binding.clBG is a View (e.g., ConstraintLayout)
            val layoutParams = holder.binding.clBG.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = 0 // Set the start margin to 10 pixels
            holder.binding.clBG.layoutParams = layoutParams // Apply the updated layout parameters

            holder.binding.ivArrow.rotation = 180f
            expand(holder.binding.llDetails)
        } else {
            animation = RotateAnimation(
                -1 * 180f,
                0f,
                RotateAnimation.RELATIVE_TO_SELF,
                0.5f,
                RotateAnimation.RELATIVE_TO_SELF,
                0.5f
            )
            holder.binding.tvShiftDays.visibility = View.VISIBLE
            // Assuming holder.binding.clBG is a View (e.g., ConstraintLayout)
            val layoutParams = holder.binding.clBG.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = 16 // Set the start margin to 10 pixels
            holder.binding.clBG.layoutParams = layoutParams // Apply the updated layout parameters
            holder.binding.cardShift.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.Orange)
            )
            collapse(holder.binding.llDetails)
            holder.binding.ivArrow.rotation = 0f
        }

        animation.duration = 500
        animation.fillAfter = true
        holder.binding.ivArrow.startAnimation(animation)
    }

    private fun expand(v: View) {
        val matchParentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                v.layoutParams.height =
                    if (interpolatedTime == 1f) LinearLayoutCompat.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }


        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

    private fun collapse(v: View) {
        val initialHeight = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

}