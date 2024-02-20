package com.casecode.nfcreader

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility object for managing coroutine scopes and launching coroutines in different contexts.
 */
object Coroutines {

    /**
     * Launches a coroutine on the Main dispatcher.
     * @param work The suspending function to be executed.
     */
    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main.immediate).launch {
            work()
        }

    /**
     * Launches a coroutine on the Main dispatcher bound to the lifecycle of the given AppCompatActivity.
     * @param activity The AppCompatActivity instance.
     * @param work The suspending function to be executed.
     */
    fun main(activity: AppCompatActivity, work: suspend ((scope: CoroutineScope) -> Unit)) =
        activity.lifecycleScope.launch {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                work(this)
            }
        }

    /**
     * Launches a coroutine on the Main dispatcher bound to the lifecycle of the given BottomSheetDialogFragment.
     * @param fragment The BottomSheetDialogFragment instance.
     * @param work The suspending function to be executed.
     */
    fun main(fragment: BottomSheetDialogFragment, work: suspend ((scope: CoroutineScope) -> Unit)) =
        fragment.lifecycleScope.launch {
            fragment.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                work(this)
            }
        }

    /**
     * Launches a coroutine on the Main dispatcher bound to the lifecycle of the given DialogFragment.
     * @param fragment The DialogFragment instance.
     * @param work The suspending function to be executed.
     */
    fun main(fragment: DialogFragment, work: suspend ((scope: CoroutineScope) -> Unit)) =
        fragment.lifecycleScope.launch {
            fragment.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                work(this)
            }
        }

    /**
     * Launches a coroutine on the Main dispatcher bound to the lifecycle of the given Fragment.
     * @param fragment The Fragment instance.
     * @param work The suspending function to be executed.
     */
    fun main(fragment: Fragment, work: suspend ((scope: CoroutineScope) -> Unit)) =
        fragment.lifecycleScope.launch {
            fragment.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                work(this)
            }
        }

    /**
     * Launches a coroutine on the IO dispatcher.
     * @param work The suspending function to be executed.
     */
    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    /**
     * Launches a coroutine on the IO dispatcher bound to the lifecycle of the given ViewModel.
     * @param viewModel The ViewModel instance.
     * @param work The suspending function to be executed.
     */
    fun io(viewModel: ViewModel, work: suspend (() -> Unit)) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            work()
        }
    }

    /**
     * Launches a coroutine on the Default dispatcher.
     * @param work The suspending function to be executed.
     */
    fun default(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Default).launch {
            work()
        }

    /**
     * Launches a coroutine on the Default dispatcher bound to the lifecycle of the given ViewModel.
     * @param viewModel The ViewModel instance.
     * @param work The suspending function to be executed.
     */
    fun default(viewModel: ViewModel, work: suspend (() -> Unit)) =
        viewModel.viewModelScope.launch(Dispatchers.Default) {
            work()
        }

    /**
     * Launches a coroutine on the Unconfined dispatcher.
     * @param work The suspending function to be executed.
     */
    fun unconfined(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Unconfined).launch {
            work()
        }
}