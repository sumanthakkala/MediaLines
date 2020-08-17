package com.sumanthakkala.medialines.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.services.BackupRestoreService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class AutoBackupWorker extends Worker {
    private Context context;
    SharedPreferences backupSharedPref;
    public AutoBackupWorker(@NonNull Context c, @NonNull WorkerParameters workerParams) {
        super(c, workerParams);
        this.context = c;
    }

    @NonNull
    @Override
    public Result doWork() {
        int result = new BackupRestoreService(context).initBackup();
        switch (result){
            case BackupRestoreService.BACKUP_FAILED:
                return Result.retry();
        }
        backupSharedPref = context.getSharedPreferences("Backup_Prefs", MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = backupSharedPref.edit();
        editor.putLong(context.getString(R.string.latest_backup_timestamp_key_in_prefs), calendar.getTimeInMillis());
        editor.apply();
        return Result.success();
    }
}
