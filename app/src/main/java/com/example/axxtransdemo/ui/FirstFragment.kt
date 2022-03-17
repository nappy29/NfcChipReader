package com.example.axxtransdemo.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.axxtransdemo.CtlessCardService
import com.example.axxtransdemo.MyViewModelFactory
import com.example.axxtransdemo.R
import com.example.axxtransdemo.data.adapter.CustomArrayAdapter
import com.example.axxtransdemo.data.model.AccessPoint
import com.example.axxtransdemo.data.model.Application
import com.example.axxtransdemo.data.model.Card
import com.example.axxtransdemo.data.model.Utility
import com.example.axxtransdemo.data.model.util.AppUtils
import com.example.axxtransdemo.data.model.util.BeepType
import com.example.axxtransdemo.databinding.FragmentFirstBinding
import com.example.axxtransdemo.viewmodel.ConnectionLiveData
import com.example.axxtransdemo.viewmodel.MainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), CtlessCardService.ResultListener {

    private var _binding: FragmentFirstBinding? = null
    lateinit var viewModel: MainViewModel

    private val TAG = FirstFragment::class.java.name
    private lateinit var PAN: String

    private var mCtlessCardService: CtlessCardService? = null

    private var mProgressDialog: ProgressDialog? = null
    private var mAlertDialog: AlertDialog? = null

    private var accessPoints: List<String>? = null
    private var accessPoint: AccessPoint? = null

    lateinit var shared : SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    protected lateinit var connectionLiveData: ConnectionLiveData


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mCtlessCardService = CtlessCardService(requireActivity(), this)

        shared = requireActivity().getSharedPreferences("pref" , Context.MODE_PRIVATE)
        editor = shared.edit()

        connectionLiveData = ConnectionLiveData(requireContext())


        viewModel = ViewModelProvider(this, MyViewModelFactory()).get(MainViewModel::class.java)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        connectionLiveData.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailable.value = it
        }

        viewModel.isNetworkAvailable.observe(viewLifecycleOwner){
            dismissProgressDialog()
            if(!it)
            showAlertDialog2("Internet Connectivity needed", "Please make sure you have internet connection")
        }

        viewModel.user.observe(viewLifecycleOwner, {

            dismissProgressDialog()

                if(it != null) {
                    var bundle = bundleOf("firstname" to it.fields.FirstName, "lastname" to it.fields.LastName)
                    findNavController().navigate(R.id.action_FirstFragment_to_thirdFragment, bundle)
                } else {
                    var bundle = bundleOf("pan" to PAN)
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
                }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Log.d("server_error_message", it)

            if(it.equals("No Internet connection"))
                showAlertDialog2("Internet Connectivity needed", "Please make sure you have internet connection")
        })

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it)
                showProgressDialogFromServer()
            else
                dismissProgressDialog()
        })

//        viewModel.getAccessPoints()
//
//        viewModel.accessPoints.observe(viewLifecycleOwner, {
//
//            Log.d("accesspoints",it.toString())
//            setupAutoCompleteView(it as ArrayList<AccessPoint>)
//        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mCtlessCardService!!.start()
    }

    override fun onCardDetect() {
        Log.d(TAG, "ON CARD DETECTED")
        playBeep(BeepType.SUCCESS)
        showProgressDialog()
    }

    override fun onCardReadSuccess(card: Card?) {
        dismissProgressDialog()
//        showCardDetailDialog(card!!)

        if (card != null) {
            PAN = card.pan.toString()
            Log.d("cardNumber", card.pan.toString())
            card.pan?.let { viewModel.getUserByToken(it) }
        }
    }

    override fun onCardReadFail(error: String?) {
        playBeep(BeepType.FAIL)
        dismissProgressDialog()
        showAlertDialog("ERROR", error!!)
    }

    override fun onCardReadTimeout() {
        playBeep(BeepType.FAIL)
        dismissProgressDialog()
        AppUtils.showSnackBar(binding.constraintL, "Timeout has been reached...", "OK")
    }

    override fun onCardMovedSoFast() {
        playBeep(BeepType.FAIL)
        dismissProgressDialog()
        AppUtils.showSnackBar(binding.constraintL, "Please do not remove your card while reading...", "OK")
    }

    override fun onCardSelectApplication(applications: List<Application?>?) {
        playBeep(BeepType.FAIL)
        dismissProgressDialog()
        showApplicationSelectionDialog(applications as List<Application>)
    }

    private fun showProgressDialog() {
        dismissAlertDialog()
        activity?.runOnUiThread {
            mProgressDialog = AppUtils.showLoadingDialog(
                requireContext(),
                "Reading Card",
                "Please do not remove your card while reading..."
            )
        }
    }

    private fun showProgressDialogFromServer() {
        dismissAlertDialog()
        activity?.runOnUiThread {
            mProgressDialog = AppUtils.showLoadingDialog(
                requireContext(),
                "Retrieving User",
                "Please wait..."
            )
        }
    }

    private fun dismissProgressDialog() {
        activity?.runOnUiThread { mProgressDialog?.dismiss() }
    }

    private fun dismissAlertDialog() {
        if (mAlertDialog != null) activity?.runOnUiThread { mAlertDialog!!.dismiss() }
    }

    private fun showAlertDialog(title: String, message: String) {
        activity?.runOnUiThread{
            mAlertDialog = AppUtils.showAlertDialog(
                requireActivity(),
                title,
                message,
                "OK",
                "SHOW APDU LOGS",
                false
            ) { _, _ -> mAlertDialog!!.dismiss() }
        }
    }

    private fun showAlertDialog2(title: String, message: String) {
        activity?.runOnUiThread{
            mAlertDialog = AppUtils.showAlertDialog(
                requireActivity(),
                title,
                message,
                "OK",
                "Cancel",
                false
            ) { _, _ -> mAlertDialog!!.dismiss() }
        }
    }

    private fun showCardDetailDialog(card: Card) {
        activity?.runOnUiThread {
            val title = "Card Detail"
            var message =
                """
            Card Brand : ${card.cardType?.cardBrand}
            Card Pan : ${card.pan.toString()}
            Card Expire Date : ${card.expireDate.toString()}
            Card Track2 Data : ${card.track2.toString()}
            
            """.trimIndent()
            if (card.emvData != null && card.emvData!!.isNotEmpty())
                message += "Card EmvData : " + card.emvData
            mAlertDialog = AppUtils.showAlertDialog(
                requireActivity(),
                title,
                message,
                "OK",
                "SHOW APDU LOGS",
                false
            ) { _, button ->
                when (button) {
                    DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEUTRAL -> {
                        mCtlessCardService!!.start()
                        mAlertDialog!!.dismiss()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
//                        if (card.getLogMessages() != null && !card.getLogMessages()
//                                .isEmpty()
//                        ) openApduLogDetail(ArrayList<Any?>(card.getLogMessages()))
//                        mAlertDialog!!.dismiss()
                    }
                }
            }
        }
    }

    private fun showApplicationSelectionDialog(applications: List<Application>) {
        val appNames = arrayOfNulls<String>(applications.size)
        for ((index, application) in applications.withIndex()) {
            appNames[index] = application.appLabel
        }
        activity?.runOnUiThread {
            val title = "Select One of Your Cards"
            mAlertDialog =
                AppUtils.showSingleChoiceListDialog(
                    requireActivity(),
                    title,
                    appNames,
                    "OK"
                ) { _, i ->
                    mCtlessCardService!!.setSelectedApplication(
                        i
                    )
                }
        }
    }

    private fun playBeep(beepType: BeepType) {
        val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        when (beepType) {
            BeepType.SUCCESS -> toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            BeepType.FAIL -> toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
        }
    }

    private fun setupAutoCompleteView(accessPoints: List<AccessPoint>) {

//        val adapter = CustomArrayAdapter(requireContext(), R.layout.list_view_row, accessPoints)
//
//        binding.autoCompleteTxt.setAdapter(adapter)
//
//        binding.autoCompleteTxt.setText(accessPoints[0].fields.Name)
//        this.accessPoint = accessPoints[0]
//        this.accessPoints = this.accessPoint!!.fields.AccessPointRight
//
//        binding.autoCompleteTxt.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, arg1, position, id ->
//
//                this.accessPoint = parent.getItemAtPosition(position) as AccessPoint
//                binding.autoCompleteTxt.setText(this.accessPoint!!.fields.Name)
//
//                this.accessPoints = this.accessPoint!!.fields.AccessPointRight
//            }

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_settings -> {
//
//
//                findNavController().navigate(
//                    R.id.action_FirstFragment_to_FourthFragment,
//                )
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}