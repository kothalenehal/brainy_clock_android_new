package com.brainyclockuser.base

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.api.ApiManager
import com.brainyclockuser.utils.NetworkUtils
import com.brainyclockuser.utils.PrefUtils
import com.brainyclockuser.utils.permission.PermissionUtil

/**
 * A simple [Fragment] subclass.
 */
abstract class BaseFragment : Fragment() {

    protected var mInputMethodManager: InputMethodManager? = null

    var apiManager: ApiManager = BrainyClockUserApp.getAppComponent().provideApiManager()
    var prefUtils: PrefUtils = BrainyClockUserApp.getAppComponent().providePrefUtil()
    var networkUtils: NetworkUtils = BrainyClockUserApp.getAppComponent().provideNetworkUtils()
    var permissionUtil: PermissionUtil =
        BrainyClockUserApp.getAppComponent().providePermissionUtil()

    open fun handleError(throwable: Throwable) {
        (activity as BaseActivity).handleError(throwable)
    }

    fun showLoader() {
        (activity as BaseActivity).showLoader()
    }

    fun hideLoader() {
        (activity as BaseActivity).hideLoader()
    }

    fun showAlert(msg: String?) {
        (activity as BaseActivity).showAlert(msg)
    }

    fun showError(msg: String?) {
        (activity as BaseActivity).showError(msg)
    }

    fun hideKeyboard(view: View?) {
        (activity as BaseActivity).hideKeyBoard(view)
    }

    fun showKeyboard(view: View?) {
        (activity as BaseActivity).showKeyBoard(view)
    }

    val isClickDisabled: Boolean
        get() = (activity as BaseActivity).isClickDisabled

    /**
     * this method calls [BaseActivity.addFragment].
     * So, it will add fragment in Activity's container
     */
    fun addFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean) {
        (activity as BaseActivity).addFragment(
            fragment,
            containerId,
            addToBackStack
        )
    }

    /**
     * this method calls [BaseActivity.replaceFragment].
     * So, it will replace fragment in Activity's container
     */
    fun replaceFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean) {
        (activity as BaseActivity).replaceFragment(
            fragment,
            containerId,
            addToBackStack
        )
    }

    /**
     * this method uses [.getChildFragmentManager] and adds nested fragment inside Fragment's container.
     * using it with activity's container will throw [IllegalStateException] or may cause other errors.
     */
    protected fun addChildFragment(
        fragment: Fragment,
        containerId: Int,
        addToBackStack: Boolean
    ) {
        val fragmentManager = this.childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
        fragmentTransaction.commit()
    }

    /**
     * this method uses [.getChildFragmentManager] and replaces nested fragment inside Fragment's container
     * using it with activity's container will throw [IllegalStateException] or may cause other errors.
     */
    protected fun replaceChildFragment(
        fragment: Fragment,
        containerId: Int,
        addToBackStack: Boolean
    ) {
        val fragmentManager = this.childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
        fragmentTransaction.commit()
    }
}