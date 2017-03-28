package hust.cc.acoustic.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by cc on 2017/3/24.
 */

public class ToastMessage {
    public static void showL(Context context, Toast mToast, String message){
        if(mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void showS(Context context, Toast mToast, String message){
        if(mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
