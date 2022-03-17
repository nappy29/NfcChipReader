package com.example.axxtransdemo.ui

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.axxtransdemo.R
import com.example.axxtransdemo.data.model.util.AppUtils
import com.example.axxtransdemo.databinding.FragmentThirdBinding

class ThirdFragment: Fragment() {

    private var timer: CountDownTimer? = null
    private var _binding: FragmentThirdBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentThirdBinding.inflate(inflater, container,false)


       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments !=null) {
            var f_name = arguments?.getString("firstname")
            var l_name = arguments?.getString("lastname")

//            var accesspointName = arguments?.getString("accessPointName")
//            if(accesspointName == null) {
//                binding.stat2.setTextColor(Color.parseColor("#FF447C4B"))
//                binding.stat2.text = "Welcome Aboard, $f_name $l_name"
//            }else{
//                binding.stat2.setTextColor(Color.parseColor("#DBD5B2"))
////                binding.stat2.text = "Sorry, $f_name $l_name, you do not have access to $accesspointName"
//                binding.stat2.text = "Sorry, $f_name $l_name, you do not have access to $accesspointName"
//            }

            binding.stat2.setTextColor(Color.parseColor("#FF447C4B"))
            binding.stat2.text = "Welcome Aboard, $f_name $l_name"
        }

        runTimer()


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

            timer?.cancel()
            findNavController().navigate(R.id.action_ThirdFragment_to_FirstFragment)
//            requireActivity().recreate()

//            AppUtils.showSnackBar(binding.constraintl, "timing out soon, please be patient", "OK")
        }
    }

    fun runTimer(){
         timer = object: CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timeoutTxt.text = "Timing out in ... " + (millisUntilFinished/1000).toString()
            }

            override fun onFinish() {
                findNavController().navigate(R.id.action_ThirdFragment_to_FirstFragment)
            }
        }

        timer?.start()
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