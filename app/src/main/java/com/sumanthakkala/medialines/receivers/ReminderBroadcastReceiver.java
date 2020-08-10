package com.sumanthakkala.medialines.receivers;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.CreateNoteActivity;
import com.sumanthakkala.medialines.entities.NoteWithData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        Display a notification to view the task details
        Intent action = new Intent(context, CreateNoteActivity.class);
        NoteWithData noteWithData = (NoteWithData) parseByteArrayToObject(intent.getByteArrayExtra("data"));
        action.putExtra("noteData",noteWithData);
        action.putExtra("isViewOrUpdate", true);

        PendingIntent operation = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        assert noteWithData != null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyMediaLines")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Media Lines Reminder")
                .setContentText((noteWithData.note.getNoteText() == null || noteWithData.note.getNoteText() == "") ? noteWithData.note.getTitle() : noteWithData.note.getNoteText())
                .setContentIntent(operation)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());
    }

    private Object parseByteArrayToObject(byte[] byteArr){
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArr);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return o;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }
}
