package io.kristal.actionpicker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltPluginWebContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActionPicker extends CobaltAbstractPlugin {

    private static final String TAG = CobaltAbstractPlugin.class.getSimpleName();

    private static ActionPicker sInstance;

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) {
            sInstance = new ActionPicker();
        }

        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        try {
            String action = message.getString("action");
            if ("getAction".equals(action)) {
                JSONObject data = message.getJSONObject(Cobalt.kJSData);
                JSONArray actionsJSON = data.getJSONArray("actions");
                int actionsLength = actionsJSON.length();
                String callback = message.getString(Cobalt.kJSCallback);

                ArrayList<String> actions = new ArrayList<>(actionsLength);
                for (int i = 0; i < actionsLength; i++){
                    actions.add(actionsJSON.getString(i));
                }

                showUIPicker(actions, callback, webContainer);
            }
            else if (Cobalt.DEBUG) {
                Log.w(TAG, "onMessage: action '" + action + "' not recognized");
            }
        }
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(TAG, "onMessage: wrong format, possible issues: \n" +
                        "\t- missing 'action' field or not a string,\n" +
                        "\t- missing 'data' field or not a object,\n" +
                        "\t- missing 'data.actions' field or not an array,\n" +
                        "\t- missing 'callback' field or not a string.\n");
            }
            exception.printStackTrace();
        }
    }

    private void showUIPicker(ArrayList<String> actions, final String callback, CobaltPluginWebContainer webContainer) {
        CharSequence[] items = actions.toArray(new CharSequence[actions.size()]);
        final CobaltFragment fragment = webContainer.getFragment();

        new AlertDialog.Builder(webContainer.getActivity())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("index", i);
                            fragment.sendCallback(callback, data);
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("index", -1);
                            fragment.sendCallback(callback, data);
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                        }
                    }
                })
                .show();
    }
}
