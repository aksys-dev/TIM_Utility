/*
 * Copyright (c) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.telecomitalia.TIMgamepad2.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.app.recommendation.ContentRecommendation;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.activity.FOTA_V2;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

//import android.content.res.Resources;

/*
 * This class builds up to MAX_RECOMMENDATIONS of ContentRecommendations and defines what happens
 * when they're selected from Recommendations section on the Home screen by creating an Intent.
 */
public class UpdateRecommendationsService extends IntentService {
    private static final String TAG = "RecommendationService";

    private NotificationManager mNotifManager;

    public UpdateRecommendationsService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (mNotifManager == null) {
                mNotifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
        } catch (Exception e) {
            Toast.makeText(UpdateRecommendationsService.this, e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Generate recommendations, but only if recommendations are enabled
       /* SharedPreferences sp = UpdateRecommendationsService.this.getSharedPreferences(CommerHelper.SPNAME, Activity.MODE_PRIVATE);
       if (!sp.getBoolean("pref_recommendation", true)) {
            Log.d(TAG, "Recommendations disabled");
            mNotifManager.cancelAll();
            return;
        }*/
        LogUtil.d(TAG, "add recommendation");
        ContentRecommendation.Builder builder = new ContentRecommendation.Builder().setBadgeIcon(R.drawable.ic_launcher);
        int id = 0;
        builder.setIdTag("0").setTitle(getString(R.string.app_name)).setText("\n" +
                "Nuovo aggiornamento disponibile").
                setContentIntentData(ContentRecommendation.INTENT_TYPE_ACTIVITY, buildPendingIntent(), 0, null);
//        Drawable drawable=UpdateRecommendationsService.this.getResources().getDrawable(R.drawable.ic_launcher);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_recommendation2);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        builder.setContentImage(bitmap);
        ContentRecommendation rec = builder.build();
        Notification notification = rec.getNotificationObject(getApplicationContext());
        mNotifManager.notify(id, notification);
    }

    private Intent buildPendingIntent() {
        Intent detailsIntent = new Intent(this, FOTA_V2.class);
        return detailsIntent;
    }


}
