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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.imchat.Constant;
import com.imchat.DemoApplication;
import com.imchat.DemoHelper;
import com.imchat.R;
import com.imchat.db.DemoDBManager;
import com.imchat.db.User;
import com.imchat.dialog.CustomLoadingDialog;
import com.imchat.utils.CustomRequest;
import com.imchat.utils.SharedPreferencesUtil;
import com.imchat.utils.UserUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Login screen
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	private static final int NOTIFY_LOGIN=1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private Dialog mDialog;
	private User user;
	private boolean autoLogin = false;

	private Handler mHandler=new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what==NOTIFY_LOGIN)
			{
				UserUtils.saveUser(getApplication(),user);
				relevanceImChat();
			}
		}
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enter the main activity if already logged in
		if (DemoHelper.getInstance().isLoggedIn()) {
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			return;
		}
		setContentView(R.layout.em_activity_login);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);

		// if user changed, clear the password
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordEditText.setText(null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))) {
					login(null);
					return true;
				}
				else{
					return false;
				}
			}
		});

		if (DemoHelper.getInstance().getCurrentUsernName() != null) {
			usernameEditText.setText(""+ SharedPreferencesUtil.get(this,"phone",""));
		}
	}

	/**
	 * login
	 * 
	 * @param view
	 */
	public void login(View view) {
		if (!EaseCommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
			return;
		}
		String currentUsername = usernameEditText.getText().toString().trim();
		String currentPassword = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(currentUsername)) {
			Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(currentPassword)) {
			Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}

		FetchLogin(currentUsername,currentPassword);

	}

	/**
	 * 登录
	 */
	private void FetchLogin(String phoneStr,String passwordStr)
	{
		mDialog= CustomLoadingDialog.setLoadingDialog(this,"");
		final Map<String,String> map=new HashMap<>();
		map.put("phone",phoneStr);
		map.put("password",passwordStr);
		CustomRequest customRequest=new CustomRequest(this, Request.Method.POST, Constant.ACCOUNT_LOGIN, map, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				if(response!=null)
				{
					Log.i("imchat",""+response.toString());
					int status=response.optInt("status");
					String msg=response.optString("msg");
					if (status==201)
					{
						JSONObject jsonObject=response.optJSONObject("data");
						int id=jsonObject.optInt("id");
						String phone=jsonObject.optString("phone");
						String token=jsonObject.optString("token");
						String i_username=jsonObject.optString("i_username");
						String i_password=jsonObject.optString("i_password");
						user=new User();
						user.id=id;
						user.nickname=i_username;
						user.psd=i_password;
						user.phone=phone;
						user.token=token;
						mHandler.sendEmptyMessage(NOTIFY_LOGIN);
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
				Toast.makeText(getApplication(),"抱歉,登录失败",Toast.LENGTH_SHORT).show();
			}
		});
		mRequestQueue.add(customRequest);
	}

	/**
	 * 关联环信
	 */
	private void relevanceImChat()
	{
		DemoDBManager.getInstance().closeDB();
		DemoHelper.getInstance().setCurrentUserName(user.nickname);

		EMClient.getInstance().login(user.nickname, user.psd, new EMCallBack() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "login: onSuccess");
				EMClient.getInstance().groupManager().loadAllGroups();
				EMClient.getInstance().chatManager().loadAllConversations();
				boolean updatenick = EMClient.getInstance().pushManager().updatePushNickname(
						DemoApplication.currentUserNick.trim());
				if (!updatenick) {
					Log.e("LoginActivity", "update current user nick fail");
				}
				// get user's info (this should be get from App's server or 3rd party service)
				DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();
				mDialog.dismiss();
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);

				finish();
			}

			@Override
			public void onProgress(int progress, String status) {
				Log.d(TAG, "login: onProgress");
			}

			@Override
			public void onError(final int code, final String message) {
				runOnUiThread(new Runnable() {
					public void run() {
						mDialog.dismiss();
						Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	 * register
	 * 
	 * @param view
	 */
	public void register(View view) {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	/**
	 * register
	 *
	 * @param view
	 */
	public void find(View view) {
		startActivityForResult(new Intent(this, FindPsdActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
}
