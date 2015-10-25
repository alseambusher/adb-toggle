package com.alse.adbtoggle;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_CLICK.equals(intent.getAction())){
            int adb = Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.ADB_ENABLED, 0);
            // toggle the USB debugging setting
            adb = adb == 0 ? 1 : 0;
            Settings.Global.putInt(context.getContentResolver(),
                    Settings.Global.ADB_ENABLED, adb);
            if (adb == 1)
                Toast.makeText(context, "Enabled ADB", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Disabled ADB", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.adb_toggle);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION_UPDATE_CLICK);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.appwidget_text, pending);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

