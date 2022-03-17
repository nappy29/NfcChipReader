package com.example.axxtransdemo.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.axxtransdemo.data.api.ApiService
import com.example.axxtransdemo.data.api.RetrofitBuilder
import com.example.axxtransdemo.data.model.AccessPoint
import com.example.axxtransdemo.data.model.User
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    var isNetworkAvailable = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val user = MutableLiveData<User>()
    val accessPoints = MutableLiveData<List<AccessPoint>>()
    val loading = MutableLiveData<Boolean>()
    val updateSuccess = MutableLiveData<Boolean>()
    private val apiService: ApiService = RetrofitBuilder.apiService
    private var mainRepository: MainRepository = MainRepository(apiService)

    var mediatorLiveData = MediatorLiveData<User>()

    fun getUserByToken(token: String){

        var tokenStr = "Token=\"$token\""

        loading.postValue(true)
    if (isNetworkAvailable.value == true) {

        viewModelScope.launch {
            val response = mainRepository.getUserByToken(tokenStr)
            withContext(Dispatchers.Main) {
                Log.d("Response_message", response.body().toString())
                if (response.body() != null && response.isSuccessful && response.body()?.records!!.isNotEmpty()) {

                    user.postValue(response.body()!!.records[0])
                    loading.postValue(false)
                } else if (response.body()?.records!!.isEmpty())
                    user.postValue(null)
                else
                    onError(response.message())
            }
        }

      }else{
            onError("No Internet connection")
      }
    }

    fun getUserByName(firstName: String, lastName: String) {
        var searchStr = "AND(FirstName=\"$firstName\",LastName=\"$lastName\")"

        Log.d("name_params", searchStr)
        loading.postValue(true)

        viewModelScope.launch {
            val resp = mainRepository.getUserByName(searchStr)

            withContext(Dispatchers.Main) {
                Log.d("Resp_message", resp.body().toString())
                if (resp.body() != null && resp.isSuccessful && resp.body()?.records!!.isNotEmpty()) {

                    user.postValue(resp.body()!!.records[0])
                    loading.postValue(false)
                } else if (resp.body()?.records!!.isEmpty())
                    user.postValue(null)
                else
                    onError(resp.message())
            }
        }
    }

        fun updateUserEntreForToken(user_id: String, token: String){
            val fields = JsonObject()
            fields.addProperty("Token", token)

            val body = JsonObject()
            body.add("fields", fields)

            updateSuccess.postValue(false)

            viewModelScope.launch {
                val response = mainRepository.updateUserEntreForToken(user_id, body)

                Log.d("body_object", body.toString())

                withContext(Dispatchers.Main){
                    Log.d("Resp_message", response.body().toString())
                    if(response.body() != null && response.isSuccessful) {

                        user.postValue(response.body()!!)
                        updateSuccess.postValue(true)
                        loading.postValue(false)
                    }
                    else {
                        updateSuccess.postValue(false)
                        onError(response.message())
                    }
                }

            }

    }

    fun getAccessPoints(){

        loading.postValue(true)

        viewModelScope.launch {
            val response = mainRepository.getAccessPoints()
            withContext(Dispatchers.Main){
                Log.d("Response_message", response.body().toString())
                if(response.body() != null && response.isSuccessful && response.body()?.records!!.isNotEmpty()) {

                    accessPoints.postValue(response.body()!!.records)
                    loading.postValue(false)
                }else if(response.body()?.records!!.isEmpty())
                    user.postValue(null)
                else
                    onError(response.message())
            }
        }
    }



    private fun onError(message: String) {
        errorMessage.postValue(message)
        loading.postValue(false)
    }
}