package com.sumanthakkala.medialines.receivers;
        import android.app.PendingIntent;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Handler;
        import android.os.HandlerThread;

        import androidx.work.ExistingPeriodicWorkPolicy;
        import androidx.work.PeriodicWorkRequest;
        import androidx.work.WorkManager;

        import com.sumanthakkala.medialines.constants.Constants;
        import com.sumanthakkala.medialines.database.MediaLinesDatabase;
        import com.sumanthakkala.medialines.entities.Reminders;
        import com.sumanthakkala.medialines.services.AlarmManagerService;
        import com.sumanthakkala.medialines.workers.ResetAlarmsWorker;

        import java.util.Calendar;
        import java.util.List;
        import java.util.concurrent.TimeUnit;

public class PackageUpdateBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                ResetAlarmsWorker.class,
                15,
                TimeUnit.MINUTES
        ).addTag("medialines_reset_alarms").build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("medialines_reset_alarms", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);


        // Display a notification to view the task details
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
                List<Reminders> reminders = MediaLinesDatabase.getMediaLinesDatabase(context).remindersDao().getAllReminders();
                // have more database operation here
                for(Reminders reminder: reminders){
                    if(!reminder.getRepeatType().equals(Constants.REMINDER_DOES_NOT_REPEAT)){
                        long nextOccuranceTimeStamp = getNextOccuringTimeStamp(reminder.getDateTimeInMillis(), reminder.getRepeatType());
                        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
                        intent.putExtra("noteId", reminder.getAssociatedNoteId());
                        intent.putExtra("repeatType", reminder.getRepeatType());
                        intent.putExtra("alarmSetupDateTimeInMillis", reminder.getDateTimeInMillis());
                        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, reminder.getAssociatedNoteId().intValue(), intent, 0);
                        AlarmManagerService.setAlarm(context, nextOccuranceTimeStamp, alarmIntent);
                    }
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

        cal.add(Calendar.YEAR, 1);
        int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        if(actualAlarmSetupTimeStamp.get(Calendar.DAY_OF_MONTH) == maximumDay){
            cal.set(Calendar.DAY_OF_MONTH, maximumDay);
        }
        return cal.getTimeInMillis();
    }
}
