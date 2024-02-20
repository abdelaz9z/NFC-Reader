package com.casecode.nfcreader.ui

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.casecode.nfcreader.Coroutines
import com.casecode.nfcreader.NFCManager
import com.casecode.nfcreader.NFCStatus
import com.casecode.nfcreader.R
import com.casecode.nfcreader.databinding.ActivityBinder
import com.casecode.nfcreader.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main activity responsible for managing NFC functionality and UI interactions.
 */
class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    NfcAdapter.ReaderCallback {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var binder: ActivityBinder? = null
    private val viewModel: MainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binder = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        binder?.viewModel = viewModel
        binder?.lifecycleOwner = this@MainActivity
        super.onCreate(savedInstanceState)
        binder?.toggleButton?.setOnCheckedChangeListener(this@MainActivity)
        Coroutines.main(this@MainActivity) { scope ->
            scope.launch(block = {
                binder?.viewModel?.observeNFCStatus()?.collectLatest(action = { status ->
                    Log.d(TAG, getString(R.string.observenfcstatus, status))
                    if (status == NFCStatus.NoOperation) NFCManager.disableReaderMode(
                        this@MainActivity,
                        this@MainActivity
                    )
                    else if (status == NFCStatus.Tap) NFCManager.enableReaderMode(
                        this@MainActivity,
                        this@MainActivity,
                        this@MainActivity,
                        viewModel.getNFCFlags(),
                        viewModel.getExtras()
                    )
                })
            })
            scope.launch(block = {
                binder?.viewModel?.observeToast()?.collectLatest(action = { message ->
                    Log.d(TAG, getString(R.string.observetoast, message))
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                })
            })
            scope.launch(block = {
                binder?.viewModel?.observeTag()?.collectLatest(action = { tag ->
                    Log.d(TAG, getString(R.string.observetag, tag))
                    binder?.textViewExplanation?.text = tag
                })
            })
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == binder?.toggleButton)
            viewModel.onCheckNFC(isChecked)
    }

    override fun onTagDiscovered(tag: Tag?) {
        binder?.viewModel?.readTag(tag)
    }

    /**
     * Launches the main fragment if it is not already added.
     */
    private fun launchMainFragment() {
        if (supportFragmentManager.findFragmentByTag(MainFragment::class.java.simpleName) == null)
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.frame_layout,
                    MainFragment.newInstance(),
                    MainFragment::class.java.simpleName
                )
                .addToBackStack(MainFragment::class.java.simpleName)
                .commit()
    }
}