package com.example.axxtransdemo.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.axxtransdemo.MyViewModelFactory
import com.example.axxtransdemo.R
import com.example.axxtransdemo.data.model.Utility
import com.example.axxtransdemo.data.model.util.AppUtils
import com.example.axxtransdemo.databinding.FragmentSecondBinding
import com.example.axxtransdemo.viewmodel.MainViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    lateinit var viewModel: MainViewModel
    private var mProgressDialog: ProgressDialog? = null

    lateinit var fName: String
    lateinit var lName: String
    private lateinit var PAN: String
    private lateinit var AccessPointName: String
    private lateinit var accessPoints: List<String>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, MyViewModelFactory()).get(MainViewModel::class.java)

        if(arguments !=null) {
             PAN = arguments?.getString("pan").toString()
//             AccessPointName = arguments?.getString("AccessPointName").toString()
//             accessPoints = arguments?.getStringArrayList("accessPoints")!!
        }

        binding.buttonSecond.setOnClickListener {

            fName = binding.fname.text.toString()
            lName = binding.lname.text.toString()

            viewModel.getUserByName(fName, lName)
//            findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
        }

        binding.cancelBtn.setOnClickListener{
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }


//        Transformations.switchMap(viewModel.user, {user -> if ()})

        viewModel.user.observe(viewLifecycleOwner, {

            dismissProgressDialog()

            if(it != null) {

                viewModel.updateUserEntreForToken(it.id, PAN)

                    var bundle = bundleOf("firstname" to fName, "lastname" to lName)
                    findNavController().navigate(
                        R.id.action_SecondFragment_to_ThirdFragment,
                        bundle
                    )

                viewModel.user.removeObservers(this)

//                var bundle = bundleOf("firstname" to fName, "lastname" to lName)
//                findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment, bundle)
            } else {
                var bundle = bundleOf("firstname" to fName, "lastname" to lName)
                findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment)
            }
        })

        viewModel.updateSuccess.observe(viewLifecycleOwner, {

            viewModel.user.removeObservers(this)

            if(it){

                dismissProgressDialog()

//                var bundle = bundleOf("firstname" to fName, "lastname" to lName)
//                findNavController().navigate(R.id.action_SecondFragment_to_ThirdFragment, bundle)
            }
            else{
//                showProgressDialogFromServer("Updating token to server", "Please wait...")
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            Log.d("server_error_message", it)
        })

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it)
                showProgressDialogFromServer("Searching User by name", "Please wait...")
            else
                dismissProgressDialog()
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)

        }
    }

    private fun showProgressDialogFromServer(title: String, msg: String) {
        activity?.runOnUiThread {
            mProgressDialog = AppUtils.showLoadingDialog(
                requireContext(),
                title,
                msg
            )
        }
    }

    private fun dismissProgressDialog() {
        activity?.runOnUiThread { mProgressDialog?.dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}