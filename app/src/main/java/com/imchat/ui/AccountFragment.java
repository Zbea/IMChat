package com.imchat.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.easemob.redpacketui.utils.RPRedPacketUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.imchat.Constant;
import com.imchat.DemoHelper;
import com.imchat.R;
import com.imchat.db.User;
import com.imchat.dialog.CustomLoadingDialog;
import com.imchat.utils.CustomRequest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by Zbea on 2017/12/7.
 */

public class AccountFragment extends Fragment implements View.OnClickListener
{
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    private ImageView headAvatar;
    private ImageView headPhotoUpdate;
    private TextView tvNickName;
    private TextView tvUsername;
    private Dialog mDialog;
    private User user;
    private static final int NOTIFY_INFO=1;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what==NOTIFY_INFO)
            {
                tvNickName.setText(user.nickname);
                tvUsername.setText(user.phone);
                if(!TextUtils.isEmpty(user.avatar)){
                    Glide.with(getActivity()).load(user.avatar).placeholder(R.drawable.em_default_avatar).into(headAvatar);
                }else{
                    Glide.with(getActivity()).load(R.drawable.em_default_avatar).into(headAvatar);
                }
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.em_fragment_account, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
        headAvatar = (ImageView)getView().findViewById(R.id.user_head_avatar);
        headPhotoUpdate = (ImageView)getView().findViewById(R.id.user_head_headphoto_update);
        tvUsername = (TextView)getView().findViewById(R.id.user_username);
        tvNickName= (TextView)getView().findViewById(R.id.user_nickname);
        getView().findViewById(R.id.ll_red).setOnClickListener(this);
        getView().findViewById(R.id.ll_set).setOnClickListener(this);
        getView().findViewById(R.id.btn_logout).setOnClickListener(this);
//        initListener() ;
        FetchInfo();
    }

    private void initListener() {
        String username = EMClient.getInstance().getCurrentUser();
        headPhotoUpdate.setVisibility(View.VISIBLE);
        headAvatar.setOnClickListener(this);
        if(username != null){
            if (username.equals(EMClient.getInstance().getCurrentUser())) {
                tvNickName.setText(EMClient.getInstance().getCurrentUser());
                EaseUserUtils.setUserAvatar(getActivity(), username, headAvatar);
            } else {
                tvNickName.setText(username);
                EaseUserUtils.setUserAvatar(getActivity(), username, headAvatar);
                asyncFetchUserInfo(username);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_red:
                //支付宝版红包SDK调用如下方法进入红包记录页面
                RPRedPacketUtil.getInstance().startRecordActivity(getActivity());
                //钱包版红包SDK调用如下方法进入零钱页面
//				RPRedPacketUtil.getInstance().startChangeActivity(getActivity());
                break;
            case R.id.ll_set:
                Intent intent = new Intent();
                intent.setClass(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.user_head_avatar:
                uploadHeadPhoto();
                break;

            case R.id.btn_logout:
                logout();
                break;
            default:
                break;
        }

    }

    private void FetchInfo()
    {
        CustomRequest customRequest=new CustomRequest(getActivity(),Constant.ACCOUNT_INFO,new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                if(response!=null)
                {
                    Log.i("imchat",""+response.toString());
                    int status=response.optInt("status");
                    if (status == 201)
                    {
                        JSONObject jsonObject=response.optJSONObject("data");
                        int id=jsonObject.optInt("id");
                        String phone=jsonObject.optString("phone");
                        String avater=jsonObject.optString("avater");
                        String nick_name= jsonObject.optString("nick_name");

                        user=new User();
                        user.id=id;
                        user.nickname=nick_name;
                        user.phone=phone;
                        user.avatar=avater;

                        mHandler.sendEmptyMessage(NOTIFY_INFO);
                    }

                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
            }
        });
        Volley.newRequestQueue(getActivity()).add(customRequest);
    }


    public void asyncFetchUserInfo(String username){
        DemoHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    DemoHelper.getInstance().saveContact(user);
                    if(getActivity().isFinishing()){
                        return;
                    }
                    if(!TextUtils.isEmpty(user.getAvatar())){
                        Glide.with(getActivity()).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(headAvatar);
                    }else{
                        Glide.with(getActivity()).load(R.drawable.em_default_avatar).into(headAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }


    void logout() {
        mDialog=CustomLoadingDialog.setLoadingDialog(getActivity(),"");
        DemoHelper.getInstance().logout(true,new EMCallBack() {

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mDialog.dismiss();
                        ((MainActivity) getActivity()).finish();
                        startActivity(new Intent(getActivity(), LoginActivity.class));

                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getActivity(), "unbind devicetokens failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void uploadHeadPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[] { getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload) },
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(getActivity(), getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK,null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            headAvatar.setImageDrawable(drawable);
            uploadUserAvatar(Bitmap2Bytes(photo));
        }

    }

    private void uploadUserAvatar(final byte[] data) {
        mDialog = CustomLoadingDialog.setLoadingDialog(getActivity(),"");
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String avatarUrl = DemoHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        if (avatarUrl != null) {
                            Toast.makeText(getActivity(), getString(R.string.toast_updatephoto_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.toast_updatephoto_fail),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).start();

    }


    public byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
