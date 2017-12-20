package com.imchat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imchat.R;


public class CustomLoadingDialog
{
	public static Dialog setLoadingDialog(final Context mContext, String msg)
	{
		final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_loading, null);
		TextView textView = (TextView) view.findViewById(R.id.loading_tv);
		textView.setText(msg);
		Dialog mDialog = new Dialog(mContext, R.style.custom_dialog);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(true);
		mDialog.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mDialog.show();
		return mDialog;
	}

}
