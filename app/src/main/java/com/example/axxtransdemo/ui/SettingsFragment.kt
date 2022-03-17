package com.example.axxtransdemo.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.axxtransdemo.MyViewModelFactory
import com.example.axxtransdemo.R
import com.example.axxtransdemo.data.adapter.CustomArrayAdapter
import com.example.axxtransdemo.data.model.AccessPoint
import com.example.axxtransdemo.data.model.util.AppUtils
import com.example.axxtransdemo.databinding.SettingPageBinding
import com.example.axxtransdemo.viewmodel.MainViewModel
import com.google.gson.Gson




class SettingsFragment: Fragment()  {


    private var _binding: SettingPageBinding? = null

    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    private var mProgressDialog: ProgressDialog? = null

    private var accessPoints: List<String>? = null
    private var accessPoint: AccessPoint? = null

    lateinit var shared : SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SettingPageBinding.inflate(inflater, container,false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, MyViewModelFactory()).get(MainViewModel::class.java)

        shared = requireActivity().getSharedPreferences("pref" , Context.MODE_PRIVATE)
        editor = shared.edit()

        viewModel.getAccessPoints()

        viewModel.accessPoints.observe(viewLifecycleOwner, {

            Log.d("accesspoints",it.toString())
            setupAutoCompleteView(it as ArrayList<AccessPoint>)
        })


        viewModel.loading.observe(viewLifecycleOwner, {
            if (it)
                showProgressDialogFromServer()
            else
                dismissProgressDialog()
        })

    }

    private fun setupAutoCompleteView(accessPoints: List<AccessPoint>) {

        val adapter = CustomArrayAdapter(requireContext(), R.layout.list_view_row, accessPoints)

        binding.autoCompleteTxt.setAdapter(adapter)

        binding.autoCompleteTxt.setText(accessPoints[0].fields.Name)
        this.accessPoint = accessPoints[0]
        this.accessPoints = this.accessPoint!!.fields.AccessPointRight

        saveToPref(this.accessPoint!!, this.accessPoints!!)

        binding.autoCompleteTxt.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->

                this.accessPoint = parent.getItemAtPosition(position) as AccessPoint
                binding.autoCompleteTxt.setText(this.accessPoint!!.fields.Name)

                this.accessPoints = this.accessPoint!!.fields.AccessPointRight

                saveToPref(this.accessPoint!!, this.accessPoints!!)
            }

    }

    fun saveToPref(accessPoint: AccessPoint, accessPoints: List<String>){

        val gson = Gson()
        var json = gson.toJson(accessPoint)

        editor.putString("accessPoint", json)
        editor.commit()

        var json2 = gson.toJson(accessPoints)
        editor.putString("accessPoints", json2)
        editor.commit()

    }

    private fun showProgressDialogFromServer() {
        activity?.runOnUiThread {
            mProgressDialog = AppUtils.showLoadingDialog(
                requireContext(),
                "Retrieving Access Point ",
                "Please wait..."
            )
        }
    }

    private fun dismissProgressDialog() {
        activity?.runOnUiThread { mProgressDialog?.dismiss() }
    }

}