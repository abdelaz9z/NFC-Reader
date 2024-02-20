package com.casecode.nfcreader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.casecode.nfcreader.Coroutines
import com.casecode.nfcreader.R
import com.casecode.nfcreader.databinding.FragmentBinder
import com.casecode.nfcreader.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main fragment responsible for displaying UI elements and handling NFC functionality.
 */
class MainFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    companion object {
        private val TAG: String = MainFragment::class.java.simpleName

        /**
         * Creates a new instance of MainFragment.
         * @return A new instance of MainFragment.
         */
        fun newInstance(): MainFragment = MainFragment()
    }

    private var binder: FragmentBinder? = null

    private val viewModel: MainViewModel by viewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binder?.viewModel = viewModel
        binder?.lifecycleOwner = this@MainFragment
        return binder?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Coroutines.main(this@MainFragment) { scope ->
            scope.launch(block = {
                binder?.viewModel?.observeToast()?.collectLatest(action = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                })
            })
            scope.launch(block = {
                binder?.viewModel?.observeTag()?.collectLatest(action = { tag ->
                    binder?.textViewExplanation?.text = tag
                })
            })
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == binder?.toggleButton)
            viewModel.onCheckNFC(isChecked)
    }
}