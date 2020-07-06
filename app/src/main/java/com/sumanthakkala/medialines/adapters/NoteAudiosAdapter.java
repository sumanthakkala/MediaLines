package com.sumanthakkala.medialines.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.listeners.NoteAudiosListener;
import com.sumanthakkala.medialines.viewmodels.NoteAudioViewModel;

import java.util.List;

public class NoteAudiosAdapter extends RecyclerView.Adapter<NoteAudiosAdapter.NoteAudioViewHolder> {

    private List<NoteAudioViewModel> noteAudiosList;
    private NoteAudiosListener audiosListener;
    private static Context context = null;

    private MediaPlayer mediaPlayer = null;
    public boolean isPlaying = false;
    private String currentAudioFileName = "";
    private NoteAudioViewHolder currentPlaybackHolder;

    private Handler seekBarHandler;
    private Runnable updateSeekBar;

    public NoteAudiosAdapter(List<NoteAudioViewModel> noteAudiosList, NoteAudiosListener noteAudiosListener) {
        this.noteAudiosList = noteAudiosList;
        this.audiosListener = noteAudiosListener;
    }

    @NonNull
    @Override
    public NoteAudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new NoteAudiosAdapter.NoteAudioViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.audio_container_layout,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteAudioViewHolder holder, final int position) {
        holder.setAudioViewData(noteAudiosList.get(position), position);
        holder.playPauseIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audiosListener.onPlayPauseCLicked(noteAudiosList.get(position).audioUniqueFileName, position);
                playPauseClicked(holder, noteAudiosList.get(position).audioUniqueFileName);
            }
        });

        holder.deleteAudioIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audiosListener.onDeleteAudioCLicked(noteAudiosList.get(position));
                currentPlaybackHolder = holder;
                if(isPlaying){
                    stopAudioPlayback();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteAudiosList.size();
    }


    private void playPauseClicked(NoteAudioViewHolder holder, String fileName){

        holder.audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(isPlaying){
                    pauseAudioPlayback();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!isPlaying){
                    int seekedProgress = seekBar.getProgress();
                    mediaPlayer.seekTo(seekedProgress);
                    resumeAudioPlayback();
                }
            }
        });

        if(!isPlaying && currentAudioFileName == fileName){
            resumeAudioPlayback();
        }
        else if(isPlaying && currentAudioFileName == fileName) {
            pauseAudioPlayback();
        }
        else if(isPlaying && currentAudioFileName != fileName){
            stopAudioPlayback();
            currentPlaybackHolder.audioSeekBar.setProgress(0);
            currentAudioFileName = fileName;
            currentPlaybackHolder = holder;
            playAudioPlayback();
        }
        else {
            currentAudioFileName = fileName;
            currentPlaybackHolder = holder;
            playAudioPlayback();
        }
    }

    private void playAudioPlayback(){
        isPlaying = true;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                currentPlaybackHolder.playPauseIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
                currentPlaybackHolder.audioSeekBar.setProgress(0);
                isPlaying = false;
            }
        });
        try {
            String fileDirPath = context.getExternalFilesDir("/").getAbsolutePath();
            String filePath = "" + fileDirPath + "/" + currentAudioFileName;
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPlaybackHolder.playPauseIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));

            currentPlaybackHolder.audioSeekBar.setMax(mediaPlayer.getDuration());
            seekBarHandler = new Handler();
            setRunnableToSeekBarHandler();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void pauseAudioPlayback(){
        isPlaying = false;
        currentPlaybackHolder.playPauseIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
        mediaPlayer.pause();
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private void resumeAudioPlayback(){
        isPlaying = true;
        currentPlaybackHolder.playPauseIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause));
        setRunnableToSeekBarHandler();
        mediaPlayer.start();
    }


    public void stopAudioPlayback(){
        isPlaying = false;
        currentPlaybackHolder.playPauseIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play));
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private void setRunnableToSeekBarHandler(){
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if(isPlaying){
                    currentPlaybackHolder.audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    seekBarHandler.postDelayed(this, 500);
                }
                else {
                    currentPlaybackHolder.audioSeekBar.setProgress(0);
                }
            }
        };
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    static class NoteAudioViewHolder extends RecyclerView.ViewHolder{

        private ImageView playPauseIV;
        private SeekBar audioSeekBar;
        private ImageView deleteAudioIV;
        public NoteAudioViewHolder(@NonNull View itemView) {
            super(itemView);

            playPauseIV = itemView.findViewById(R.id.playPauseIV);
            audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
            deleteAudioIV = itemView.findViewById(R.id.deleteAudioIV);

        }

        void setAudioViewData(NoteAudioViewModel noteAudioViewModel, int position){
            if(noteAudioViewModel.audioUniqueFileName != null){

            }
        }
    }
}
