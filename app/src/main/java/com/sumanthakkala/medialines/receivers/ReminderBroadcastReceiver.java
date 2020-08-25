package com.sumanthakkala.medialines.receivers;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.CreateNoteActivity;
import com.sumanthakkala.medialines.constants.Constants;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.sumanthakkala.medialines.services.AlarmManagerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        // Display a notification to view the task details
        final long noteId = intent.getLongExtra("noteId", -1);
        assert noteId != -1;
        HandlerThread handlerThread =  new HandlerThread("database_helper");
        handlerThread.start();
        Handler handler =  new Handler(handlerThread.getLooper());
/*
IT IS A BAD PRACTICE TO PERFORM DB I/O ON BROADCAST RECEIVER SINCE ON_RECEIVE WILL BE EXECUTING ON MAIN THREAD.
SO, WE CREATED A NEW THREAD TO FETCH NOTE DATA AND THEN DISPLAYING NOTIFICATION TO USER.
*/
        handler.post(new Runnable() {
            @Override
            public void run() {
                NoteWithData currentNoteWithData = MediaLinesDatabase.getMediaLinesDatabase(context).noteDao().getNoteWithDataByNoteId(noteId);
                // have more database operation here
                Intent action = new Intent(context, CreateNoteActivity.class);
                action.putExtra("noteData",currentNoteWithData);
                action.putExtra("isViewOrUpdate", true);

                PendingIntent operation = TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(action)
                        .getPendingIntent((int) noteId, 0);

                String notificationText = (currentNoteWithData.note.getNoteText() == null || currentNoteWithData.note.getNoteText().isEmpty()) ? currentNoteWithData.note.getTitle() : currentNoteWithData.note.getNoteText();
                assert currentNoteWithData != null;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyMediaLines")
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Media Lines Reminder")
                        .setContentText(notificationText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                        .setContentIntent(operation)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify((int) noteId, builder.build());


                //If alarm is repeating, creating next alarm
                String repeatType = intent.getStringExtra("repeatType");
                Long alarmSetupDateTimeInMillis = intent.getLongExtra("alarmSetupDateTimeInMillis", -1);
                if(!repeatType.equals(Constants.REMINDER_DOES_NOT_REPEAT)){
                    Long nextMontTimeStamp = getNextOccuringTimeStamp(alarmSetupDateTimeInMillis, repeatType);

                    Intent newIntent = new Intent(context, ReminderBroadcastReceiver.class);
                    newIntent.putExtra("noteId", currentNoteWithData.note.getNoteId());
                    newIntent.putExtra("repeatType", repeatType);
                    newIntent.putExtra("alarmSetupDateTimeInMillis", alarmSetupDateTimeInMillis);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context, (int) noteId, newIntent, 0);

                    AlarmManagerService.setAlarm(context, nextMontTimeStamp, alarmIntent);
                }
            }
        });
    }

    private long getNextOccuringTimeStamp(long alarmSetupDateTimeInMillis, String repeatType){
        switch (repeatType){
            case Constants.REMINDER_MONTHLY:
                return getNextMonthDateTimeInMillis(alarmSetupDateTimeInMillis);
            case Constants.REMINDER_YEARLY:
                return getNextYearDateTimeInMillis(alarmSetupDateTimeInMillis);
            case Constants.REMINDER_DAILY:
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(alarmSetupDateTimeInMillis);
                Calendar currentStamp = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, currentStamp.get(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.MONTH, currentStamp.get(Calendar.MONTH));
                cal.set(Calendar.YEAR, currentStamp.get(Calendar.YEAR));
                cal.add(Calendar.DAY_OF_MONTH, 1);
                return cal.getTimeInMillis();
            case Constants.REMINDER_WEEKLY:
                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(alarmSetupDateTimeInMillis);
                Calendar currentStamp1 = Calendar.getInstance();
                cal1.set(Calendar.DAY_OF_MONTH, currentStamp1.get(Calendar.DAY_OF_MONTH));
                cal1.set(Calendar.MONTH, currentStamp1.get(Calendar.MONTH));
                cal1.set(Calendar.YEAR, currentStamp1.get(Calendar.YEAR));
                cal1.add(Calendar.DAY_OF_MONTH, 7);
                return cal1.getTimeInMillis();
            default:
                return -1;
        }
    }

    private long getNextMonthDateTimeInMillis(long alarmSetupDateTimeInMillis){
        // actual alarm setup date time
        Calendar actualAlarmSetupTimeStamp = Calendar.getInstance();
        actualAlarmSetupTimeStamp.setTimeInMillis(alarmSetupDateTimeInMillis);
        // get todays date
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(alarmSetupDateTimeInMillis);
        Calendar currentStamp = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, currentStamp.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.MONTH, currentStamp.get(Calendar.MONTH));
        cal.set(Calendar.YEAR, currentStamp.get(Calendar.YEAR));
        // reset calendar to next month -- This will increment year as well
        cal.add(Calendar.MONTH, 1);
        // get the maximum possible days in this month
        int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        if(actualAlarmSetupTimeStamp.get(Calendar.DAY_OF_MONTH) > maximumDay){
            // set the calendar to maximum day (e.g in case of fEB 28th, or leap 29th)
            cal.set(Calendar.DAY_OF_MONTH, maximumDay);
        }
        else {
            cal.set(Calendar.DAY_OF_MONTH, actualAlarmSetupTimeStamp.get(Calendar.DAY_OF_MONTH));
        }

        long thenTime = cal.getTimeInMillis(); // this is time one month ahead
        return (thenTime); // this is what you set as trigger point time i.e one month after

    }

    private long getNextYearDateTimeInMillis(long alarmSetupDateTimeInMillis){
        // actual alarm setup date time
        Calendar actualAlarmSetupTimeStamp = Calendar.getInstance();
        actualAlarmSetupTimeStamp.setTimeInMillis(alarmSetupDateTimeInMillis);
        // get todays date
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(alarmSetupDateTimeInMillis);
        Calendar currentStamp = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, currentStamp.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.MONTH, currentStamp.get(Calendar.MONTH));
        cal.set(Calendar.YEAR, currentStamp.get(Calendar.YEAR));
        // reset calendar to next year -- This will increment year as well
        cal.add(Calendar.YEAR, 1);
        int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        if(actualAlarmSetupTimeStamp.get(Calendar.DAY_OF_MONTH) == maximumDay){
            cal.set(Calendar.DAY_OF_MONTH, maximumDay);
        }
        return cal.getTimeInMillis();
    }
//
//    private Object parseByteArrayToObject(byte[] byteArr){
//        ByteArrayInputStream bis = new ByteArrayInputStream(byteArr);
//        ObjectInput in = null;
//        try {
//            in = new ObjectInputStream(bis);
//            Object o = in.readObject();
//            return o;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//        }
//        return null;
//    }
//
//    private byte[] parseObjectToByteArray(Object o){
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutputStream out = null;
//        try {
//            out = new ObjectOutputStream(bos);
//            out.writeObject((NoteWithData) o);
//            out.flush();
//            byte[] yourBytes = bos.toByteArray();
//            return yourBytes;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                bos.close();
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//        }
//        return null;
//    }
}