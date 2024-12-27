package com.brainyclockuser.base

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.api.ApiManager
import com.brainyclockuser.utils.PrefUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {

    var loading: MutableLiveData<Boolean> = MutableLiveData()
    var throwable: MutableLiveData<Throwable> = MutableLiveData()
    var tempClick: MutableLiveData<Boolean> = MutableLiveData()

    var apiManager: ApiManager = BrainyClockUserApp.getAppComponent().provideApiManager()
    var prefUtils: PrefUtils = BrainyClockUserApp.getAppComponent().providePrefUtil()
}


@BindingAdapter("app:tint")
fun setAppTint(view: ImageView, tintColor: Int) {
    ImageViewCompat.setImageTintList(view, ColorStateList.valueOf(tintColor))
}


@BindingAdapter("android:layout_margin")
fun setMargin(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        Math.round(margin), Math.round(margin),
        Math.round(margin), Math.round(margin)
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_marginRight")
fun setMarginEnd(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, layoutParams.leftMargin,
        Math.round(margin), layoutParams.leftMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_marginStart")
fun setMarginStart(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        margin.roundToInt(), layoutParams.leftMargin,
        layoutParams.leftMargin, layoutParams.leftMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_marginTop")
fun setMarginTop(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, Math.round(margin),
        layoutParams.leftMargin, layoutParams.leftMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_marginEnd")
fun ViewGroup.setMarginEndValue(marginValue: Float) =
    (layoutParams as ViewGroup.MarginLayoutParams).apply { rightMargin = marginValue.toInt() }

@BindingAdapter("android:layout_marginBottom")
fun setMarginBottom(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, layoutParams.leftMargin,
        layoutParams.leftMargin, Math.round(margin)
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_marginVertical")
fun setMarginVertical(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        layoutParams.leftMargin, margin.roundToInt(),
        layoutParams.rightMargin, margin.roundToInt()
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("android:layout_marginHorizontal")
fun setMarginHorizontal(view: View, margin: Float) {
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(
        margin.roundToInt(), layoutParams.topMargin,
        margin.roundToInt(), layoutParams.bottomMargin
    )
    view.layoutParams = layoutParams
}

@BindingAdapter("app:indicatorColor")
fun setIndicatorColor(view: LinearProgressIndicator, color: Int) {
    view.setIndicatorColor(color)

}

@BindingAdapter("app:strokeWidth")
fun setStrokeWidth(view: MaterialCardView, width: Float) {
    view.strokeWidth = width.toInt()

}