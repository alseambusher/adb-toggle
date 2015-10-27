package com.alse.adbtoggle;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.Layout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implementation of App Widget functionality.
 */
public class AdbToggle extends AppWidgetProvider {

    private String ACTION_UPDATE_CLICK = "com.alse.adbtoggle.action.UPDATE_CLICK";
    private String WRITE_PERMISSION = "android.permission.WRITE_SECURE_SETTINGS";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Toast.makeText(context, "ADB Toggle Widget added", Toast.LENGTH_SHORT).show();
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("pm grant "+context.getPackageName()+" "+WRITE_PERMISSION+"\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private boolean isAdbOn(Context context){
        if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1)
            return true;
        return false;
    }
    private void toggleAdb(Context context){
        int adb = isAdbOn(context) ? 0 : 1;
        Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, adb);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_CLICK.equals(intent.getAction())){

            toggleAdb(context);
            ComponentName cn = new ComponentName(context, AdbToggle.class);
            for (int appWidgetID : AppWidgetManager.getInstance(context).getAppWidgetIds(cn)){
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetID);
            }
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), isAdbOn(context) ? R.layout.adb_toggle : R.layout.adb_toggle_off);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewText(R.id.appwidget_state, isAdbOn(context) ? "ON" : "OFF");

        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_UPDATE_CLICK);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.appwidget_layout, pending);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

