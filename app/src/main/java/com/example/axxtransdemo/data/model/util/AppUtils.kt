package com.example.axxtransdemo.data.model.util

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.google.android.material.snackbar.Snackbar

object AppUtils {
    fun showLoadingDialog(context: Context?, title: String?, message: String?): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(title)
        progressDialog.setMessage(message)
        progressDialog.isIndeterminate = true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        return progressDialog
    }

    fun showAlertDialog(
        activity: Activity?,
        title: String?,
        message: String?,
        positiveBtnText: String?,
        negativeBtnText: String?,
        isCancelable: Boolean,
        listener: DialogInterface.OnClickListener?
    ): AlertDialog {
        val alertDialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveBtnText, listener)
            .setCancelable(isCancelable)
        if (negativeBtnText != null && !negativeBtnText.isEmpty()) alertDialog.setNegativeButton(
            negativeBtnText,
            listener
        )
        return alertDialog.show()
    }

    fun showSnackBar(containerView: View?, message: String?, buttonText: String?): Snackbar {
        val snackbar = Snackbar.make(containerView!!, message!!, Snackbar.LENGTH_LONG)
        snackbar.setAction(buttonText) { view: View? -> snackbar.dismiss() }
        snackbar.duration = 2500
        snackbar.show()
        return snackbar
    }

    fun showSingleChoiceListDialog(
        activity: Activity?,
        title: String?,
        list: Array<String?>?,
        positiveBtnText: String?,
        listener: DialogInterface.OnClickListener?
    ): AlertDialog {
        // setup the alert builder
        val alertDialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setPositiveButton(positiveBtnText, null)
            .setCancelable(false)
        alertDialog.setSingleChoiceItems(list, -1, listener)
        return alertDialog.show()
    }
}
