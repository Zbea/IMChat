/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imchat.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.imchat.Constant;
import com.imchat.R;
import com.imchat.db.User;
import com.imchat.dialog.CustomLoadingDialog;
import com.imchat.utils.CustomRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * register screen
 * 
 */
public class FindPsdActivity extends BaseActivity {
	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private TextView tvCode;

	private int initTime=60;
	private Timer mTimer = new Timer();
	private Dialog mDialog;
	private User user;
	private static final int NOTIFY_CODE=1;
	private static final int NOTIFY_REGISTER=2;

	private Handler mHandler=new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what==NOTIFY_CODE)
			{
				codeTimer();
			}
			if (msg.what==NOTIFY_REGISTER)
			{
				finish();
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_find);
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);
		tvCode=(TextView) findViewById(R.id.tv_code);
	}

	/**
	 * 验证码倒计时
	 */
	private void codeTimer() {
		tvCode.setBackgroundResource(R.drawable.btn_no_gary_click);
		tvCode.setText( initTime + "s");
		tvCode.setClickable(false);
		mTimer.schedule(new MyTimer(), 1000, 1000);
	}

	/**
	 * 倒计时
	 */
	private class MyTimer extends TimerTask
	{
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					initTime = initTime - 1;
					tvCode.setText(initTime + "s");
					if (initTime <= 0) {
						mTimer.cancel();
						tvCode.setText("获取验证码");
						tvCode.setBackgroundResource(R.drawable.em_btn_style_alert_dialog_special);
						tvCode.setClickable(true);
					}
				}
			});
		}
	}

	/**
	 * 获取验证码
	 */
	public void fetchCode(View view)
	{
		final String username = userNameEditText.getText().toString().trim();
		if (TextUtils.isEmpty(username)) {
			Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
			userNameEditText.requestFocus();
			return;
		}
		CustomRequest customRequest=new CustomRequest(this, Constant.ACCOUNT_GET_CODE +"2&phone="+ username, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				if (response==null)
					return;
				int status=response.optInt("status");
				if (status==201)
				{
					mHandler.sendEmptyMessage(NOTIFY_CODE);
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
			}
		});
		mRequestQueue.add(customRequest);
	}




	public void find(View view) {
		final String username = userNameEditText.getText().toString().trim();
		final String pwd = passwordEditText.getText().toString().trim();
		String confirm_pwd = confirmPwdEditText.getText().toString().trim();
		if (TextUtils.isEmpty(username)) {
			Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
			userNameEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(pwd)) {
			Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
			passwordEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(confirm_pwd)) {
			Toast.makeText(this, getResources().getString(R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
			confirmPwdEditText.requestFocus();
			return;
		}

		FetchRegister(username,pwd,confirm_pwd);

	}


	/**
	 * 注册
	 */
	private void FetchRegister(String phoneStr,String passwordStr,String codeStr)
	{
		mDialog= CustomLoadingDialog.setLoadingDialog(this,"");
		final Map<String,String> map=new HashMap<>();
		map.put("phone",phoneStr);
		map.put("password",passwordStr);
		map.put("vcode",codeStr);
		CustomRequest customRequest=new CustomRequest(this, Request.Method.POST, Constant.ACCOUNT_FIND_PSD, map, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				if (mDialog!=null)
					mDialog.dismiss();
				if(response!=null)
				{
					Log.i("imchat",""+response.toString());
					int status=response.optInt("status");
					String msg=response.optString("msg");
					if (status==201)
					{
//						JSONObject jsonObject=response.optJSONObject("data");
//						String phone=jsonObject.optString("phone");
//						String token=jsonObject.optString("token");
//						String i_username=jsonObject.optString("i_username");
//						String i_password=jsonObject.optString("i_password");
//						user=new User();
//						user.nickname=i_username;
//						user.psd=i_password;
//						user.phone=phone;
//						user.token=token;
						mHandler.sendEmptyMessage(NOTIFY_REGISTER);
					}
					else
					{
						if (mDialog!=null)
							mDialog.dismiss();
						Toast.makeText(getApplication(),msg,Toast.LENGTH_SHORT).show();
					}
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (mDialog!=null)
					mDialog.dismiss();
				Toast.makeText(getApplication(),"抱歉,找回密码失败",Toast.LENGTH_SHORT).show();
			}
		});
		mRequestQueue.add(customRequest);
	}


//    /**
//     * 关联环信
//	 */
//	private void relevanceImChat()
//	{
//		new Thread(new Runnable() {
//			public void run() {
//				try {
//					// call method in SDK
//					EMClient.getInstance().createAccount(user.nickname, user.psd);
//					runOnUiThread(new Runnable() {
//						public void run() {
//							if (!FindPsdActivity.this.isFinishing())
//								mDialog.dismiss();
//							// save current user
//							DemoHelper.getInstance().setCurrentUserName(user.nickname);
//							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
//							finish();
//						}
//					});
//				} catch (final HyphenateException e) {
//					runOnUiThread(new Runnable() {
//						public void run() {
//							if (!FindPsdActivity.this.isFinishing())
//								mDialog.dismiss();
//							int errorCode=e.getErrorCode();
//							if(errorCode==EMError.NETWORK_ERROR){
//								Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
//							}else if(errorCode == EMError.USER_ALREADY_EXIST){
//								Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
//							}else if(errorCode == EMError.USER_AUTHENTICATION_FAILED){
//								Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
//							}else if(errorCode == EMError.USER_ILLEGAL_ARGUMENT){
//								Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
//							}else{
//								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
//							}
//						}
//					});
//				}
//			}
//		}).start();
//	}

	public void back(View view) {
		finish();
	}

}
