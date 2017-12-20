package com.imchat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.imchat.db.User;


/**
 * Created by Zbea on 2017/6/22. 保存账户信息
 */

public class UserUtils
{
    private static final String SPName="IMChat";
    public static void saveUser(Context context, User user){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("userId",user.id);
        editor.putString("token",user.token);
        editor.putString("phone",user.phone);
        editor.putString("avatar",user.avatar);
        editor.putString("psd",user.psd);
        editor.putString("nickName",user.nickname);
        editor.commit();
    }
    public static User getUser(Context context){
        int userId = (Integer)SharedPreferencesUtil.get(context, "userId", -1);
        String token = (String) SharedPreferencesUtil.get(context, "token", "");
        String avatar = (String) SharedPreferencesUtil.get(context, "avatar", "");
        String nickName = (String) SharedPreferencesUtil.get(context, "nickName", "");
        String phone = (String) SharedPreferencesUtil.get(context, "phone", "");
        String psd = (String) SharedPreferencesUtil.get(context, "psd", "");
        User user=new User();
        user.id=userId;
        user.token=token;
        user.nickname=nickName;
        user.avatar=avatar;
        user.phone=phone;
        user.psd=psd;
        return user;
    }

    public static void clearUser(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences
                (SPName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("userId",-1);
        editor.putString("token","");
        editor.putString("avatar","");
        editor.putString("nickName","");
        editor.putString("phone","");
        editor.putString("psd","");
        editor.commit();
    }

}
