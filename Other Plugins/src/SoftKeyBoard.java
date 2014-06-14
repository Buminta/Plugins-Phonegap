package org.plugins;

/**
 * Created with IntelliJ IDEA.
 * User: Buminta
 * Date: 12/5/13
 * Time: 8:00 PM
 * To change this template use File | Settings | File Templates.
 */
import org.json.JSONArray;
import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
public class SoftKeyBoard extends Plugin {
    public SoftKeyBoard() {
    }

    public void showKeyBoard() {
        try {
            //use this for PhoneGape version before 2.0: InputMethodManager mgr = (InputMethodManager) this.ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodManager mgr = (InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(webView, InputMethodManager.SHOW_IMPLICIT);

            //use this for PhoneGape version before 2.0: ((InputMethodManager) this.ctx.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(webView, 0);
            ((InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(webView, 0);

            // this code will send a "DELETE" keypress to your page, thus making your input active and moving caret inside it
            // replace cordova.getActivity() by this.ctx  if running PhoneGap before version 2.0
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    webView.dispatchKeyEvent(new KeyEvent( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL ));
                    webView.dispatchKeyEvent(new KeyEvent( KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL ));
                }
            });

        }
        catch (Exception ex){
//            Log.d("Error: "+ex.getMessage());
        }
    }

    public void hideKeyBoard() {
        //use this for PhoneGape version before 2.0: InputMethodManager mgr = (InputMethodManager) this.ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodManager mgr = (InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(webView.getWindowToken(), 0);
    }

    public boolean isKeyBoardShowing() {

        int heightDiff = webView.getRootView().getHeight() - webView.getHeight();
        return (100 < heightDiff); // if more than 100 pixels, its probably a keyboard...
    }

    public PluginResult execute(String action, JSONArray args, String callbackId) {
        if (action.equals("show")) {
            this.showKeyBoard();
            return new PluginResult(PluginResult.Status.OK, "done");
        }
        else if (action.equals("hide")) {
            this.hideKeyBoard();
            return new PluginResult(PluginResult.Status.OK);
        }
        else if (action.equals("isShowing")) {

            return new PluginResult(PluginResult.Status.OK, this.isKeyBoardShowing());
        }
        else {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
    }
}
