package com.sumanthakkala.medialines.services;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.sumanthakkala.medialines.database.MediaLinesDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static android.os.Environment.getExternalStorageDirectory;

public class BackupRestoreService {
    public static final int BACKUP_SUCCESS = 1;
    public static final int BACKUP_FAILED = 2;
    public static final int RESTORE_FAILED = 3;
    public static final int RESTORE_SUCCESS = 4;
    public static final int INVALID_BACKUP_FILE = 5;

    private Context context;

    public BackupRestoreService(Context c){
        this.context = c;
    }
    public int initBackup(){
        MediaLinesDatabase appDatabase = MediaLinesDatabase.getMediaLinesDatabase(context);
        appDatabase.close();
        File dbfile = context.getDatabasePath("media_lines_database");
        File backupFilesDir = new File(getExternalStorageDirectory(), "MediaLines/Backups");
        String fileName = "backup_db";

        String sfpath = backupFilesDir.getPath() + File.separator + fileName;
        if (!backupFilesDir.exists()) {
            backupFilesDir.mkdirs();
        }

        checkAndDeleteOldBackups();

        File savefile = new File(backupFilesDir, fileName);
        if (savefile.exists()) {
            savefile.delete();
        }
        try {
            if (savefile.createNewFile()) {
                int buffersize = 8 * 1024;
                byte[] buffer = new byte[buffersize];
                int bytes_read = buffersize;
                OutputStream savedb = new FileOutputStream(sfpath);
                InputStream indb = new FileInputStream(dbfile);
                while ((bytes_read = indb.read(buffer, 0, buffersize)) > 0) {
                    savedb.write(buffer, 0, bytes_read);
                }
                savedb.flush();
                indb.close();
                savedb.close();
                return zipBackupWithMedia(savefile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return BACKUP_FAILED;
        }
        return BACKUP_SUCCESS;
    }

    public int restoreBackup(Uri fileUri){
        try {
            String fileZip = fileUri.getPath();
            File backupFilesDir = new File(getExternalStorageDirectory(), "MediaLines/Backups");
            String[] arr = fileUri.getPath().split("/");
            File zipFile = new File(backupFilesDir, arr[arr.length - 1]);
            File destDir = new File(context.getExternalCacheDir().getAbsolutePath() + "/Restore");
            if(!destDir.exists()){
                destDir.mkdirs();
            }
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            return restoreDatabase(destDir);
        }
        catch (Exception e){
            e.printStackTrace();
            return RESTORE_FAILED;
        }
    }

    private void checkAndDeleteOldBackups(){
        File backupFilesDir = new File(getExternalStorageDirectory(), "MediaLines/Backups");
        File[] filesArr = backupFilesDir.listFiles();
        List<File> files = new ArrayList<File>();
        Collections.addAll(files, filesArr);
        for(File file: files){
            if(file.isDirectory()){
                files.remove(file);
            }
        }
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File object1, File object2) {
                return (int) (Math.max(object1.lastModified(), object2.lastModified()));
            }
        });
        if(files.size() > 6){
            for(File deleteFile: files.subList(6, files.size())){
                deleteFile.delete();
            }
        }
    }

    private int zipBackupWithMedia(File backupDb){
        File backupFilesDir = new File(getExternalStorageDirectory(), "MediaLines/Backups");
        String fileName = "backup_" + getDateStringFroBackup();
        String filePath = backupFilesDir.getPath() + File.separator + fileName;
        try {
            FileOutputStream fos = new FileOutputStream(filePath + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(backupDb.getAbsolutePath());
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            File[] children = context.getExternalFilesDir("/").listFiles();

            for(File child: children){
                FileInputStream fis1 = new FileInputStream(child);
                ZipEntry zipEntry1 = new ZipEntry(child.getName());
                zipOut.putNextEntry(zipEntry1);

                byte[] bytes1 = new byte[1024];
                int length1;
                while((length1 = fis1.read(bytes1)) >= 0) {
                    zipOut.write(bytes1, 0, length1);
                }
                fis1.close();
            }
            zipOut.close();
            fos.close();
            backupDb.delete();
            return BACKUP_SUCCESS;
        }
        catch (Exception e){
            e.printStackTrace();
            return BACKUP_FAILED;
        }
    }


    private String  getDateStringFroBackup(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String dateString = format.format( new Date()   );
        return dateString;
    }


    private int restoreDatabase(File destDir){
        //Restoring Database
        try {
            File databaseBackupFile = new File(destDir, "backup_db");
            Uri fileUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider",
                    databaseBackupFile);
            InputStream inputStreamNewDB = context.getContentResolver().openInputStream(fileUri);
            if (validFile(fileUri)) {
                MediaLinesDatabase appDatabase = MediaLinesDatabase.getMediaLinesDatabase(context);
                appDatabase.close();
                File oldDB = context.getDatabasePath("media_lines_database");
                if (inputStreamNewDB != null) {
                    try {
                        copyDb((FileInputStream) inputStreamNewDB, new FileOutputStream(oldDB));
                        databaseBackupFile.delete();
                        return restoreMediaFromBackup(destDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                        inputStreamNewDB.close();
                        return RESTORE_FAILED;
                    }
                } else {
                    return INVALID_BACKUP_FILE;
                }
            } else {
                return INVALID_BACKUP_FILE;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return RESTORE_FAILED;
        }
    }

    private int restoreMediaFromBackup(File destDir){
        File existinfFiles = new File(context.getExternalFilesDir("/").getAbsolutePath());
        existinfFiles.delete();
        existinfFiles.mkdirs();
        File[] files = destDir.listFiles();
        for(File file: files){
            try {
                File newFile = new File(existinfFiles, file.getName());
                newFile.createNewFile();
                copyFile(file, newFile);
                file.delete();
            }
            catch (Exception e){
                e.printStackTrace();
                return RESTORE_FAILED;
            }
        }
        return RESTORE_SUCCESS;
    }

    private void copyFile(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    private void copyDb(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private boolean validFile(Uri fileUri) {
        ContentResolver cr = context.getContentResolver();
        String mime = cr.getType(fileUri);
        return "application/octet-stream".equals(mime);
    }
}
