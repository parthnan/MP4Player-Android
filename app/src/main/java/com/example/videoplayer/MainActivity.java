package com.example.videoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String activityState = getIntent().getStringExtra(VIDEO_ACTIVITY_INTENT);
        if(activityState!=null&&activityState.equals("showContinueWatching")){
            final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final String videoPath = SP.getString("continueWatching",null);
            if(videoPath==null){
                return;
            }
            final Button continueWatchingButton = findViewById(R.id.continueWatchingButton);
            continueWatchingButton.setVisibility(View.VISIBLE);
            continueWatchingButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    final Intent intent = new Intent (MainActivity.this, VidPlayer.class);
                    intent.putExtra(VIDEO_ACTIVITY_INTENT,"continueWatching");
                    startActivity(intent);
                }
            });
        }
    }

    private static final int REQUEST_PERMISSIONS = 1234;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
        for(int i=0 ; i<PERMISSIONS_COUNT ; i++){
            if(checkSelfPermission(PERMISSIONS[i])!= PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,final int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode == REQUEST_PERMISSIONS&&grantResults.length > 0){
            if(arePermissionsDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }else{
                onResume();
            }
        }
    }

    private List<String> videosList;

    private void addVideosFrom(String dirPath){
        final File videosDir = new File(dirPath);
        if(!videosDir.exists()){
            videosDir.mkdir();
            return;
        }
        final File[] files = videosDir.listFiles();
        for(File file: files){
            final String path = file.getAbsolutePath();
            if(path.endsWith(".mp4")){
                videosList.add(path);
            }
        }
    }

    private void fillVideosList(){
        videosList.clear();
        addVideosFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)));
        addVideosFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
        addVideosFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)));
    }

    private boolean isVideoPlayerInitialized;

    private boolean isLongClick;

    public static final String VIDEO_ACTIVITY_INTENT ="video";

    private boolean isSelected;

    private int selectedPosition;

    private View selectedView;

    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
            return;
        }

        if(!isVideoPlayerInitialized){
            final ListView listView = findViewById(R.id.listView);
            final TextAdapter textAdapter = new TextAdapter();
            videosList = new ArrayList<>();
            fillVideosList();
            textAdapter.setData(videosList);
            listView.setAdapter(textAdapter);


            final VideoView videoview = findViewById(R.id.videoView);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                    if(isLongClick){
                        isLongClick=false;
                        return;
                    }
                    Intent intent = new Intent(MainActivity.this, VidPlayer.class);
                    intent.putExtra(VIDEO_ACTIVITY_INTENT,videosList.get(position));
                    startActivity(intent);
                }
            });

            final View bottomBar = findViewById(R.id.bottomBar);

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    isLongClick=true;
                    if(isSelected){
                        if(selectedPosition!=position){
                            return false;
                        }
                        view.setBackgroundColor(Color.WHITE);
                        isSelected=false;
                        bottomBar.setVisibility(View.GONE);
                    }else{
                        selectedPosition=position;
                        view.setBackgroundColor(Color.LTGRAY);
                        isSelected=true;
                        selectedView=view;
                        bottomBar.setVisibility(View.VISIBLE);
                    }

                    return false;
                }
            });

            final Button rename = findViewById(R.id.rename);
            final Button delete = findViewById(R.id.delete);
            final Button share = findViewById(R.id.share);

            rename.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    final AlertDialog.Builder renameDialog =
                            new AlertDialog.Builder(MainActivity.this);
                    renameDialog.setTitle("Rename To:");
                    final EditText input = new EditText(MainActivity.this);
                    final String renamePath = videosList.get(selectedPosition);
                    input.setText(renamePath.substring(renamePath.lastIndexOf('/')));
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    renameDialog.setView(input);
                    renameDialog.setPositiveButton("Rename",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            String s = new File(renamePath).getParent()+"/"+input.getText();
                            File newFile = new File(s);
                            new File(renamePath).renameTo(newFile);
                            fillVideosList();
                            textAdapter.setData(videosList);
                            selectedView.setBackgroundColor(Color.WHITE);
                            isSelected=false;
                            bottomBar.setVisibility(View.GONE);
                        }
                    });
                    renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            selectedView.setBackgroundColor(Color.WHITE);
                            isSelected=false;
                            bottomBar.setVisibility(View.GONE);
                        }
                    });
                    renameDialog.show();
                }
            });

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            share.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    String videoPath = videosList.get(selectedPosition);
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(videoPath)));
                    startActivity(Intent.createChooser(intent,"Share Video To:"));
                    selectedView.setBackgroundColor(Color.WHITE);
                    isSelected = false;
                    bottomBar.setVisibility(View.GONE);
                }
            });

            isVideoPlayerInitialized = true;

            delete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("Delete");
                    deleteDialog.setMessage("Do you want to delete the video?");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new File(videosList.get(selectedPosition)).delete();
                            fillVideosList();
                            textAdapter.setData(videosList);
                            selectedView.setBackgroundColor(Color.WHITE);
                            isSelected=false;
                            bottomBar.setVisibility(View.GONE);
                        }
                    });
                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            selectedView.setBackgroundColor(Color.WHITE);
                            isSelected=false;
                            bottomBar.setVisibility(View.GONE);
                        }
                    });
                    deleteDialog.show();
                }
            });
        }
    }

    class TextAdapter extends BaseAdapter{

        private List<String> data = new ArrayList<>();

        public void setData(List<String> mData){
            data.clear();
            data.addAll(mData);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
                convertView.setTag(new ViewHolder((TextView)convertView.findViewById(R.id.myItem)));
            }
            ViewHolder holder = (ViewHolder)convertView.getTag();
            final String item = data.get(position);
            holder.info.setText(item.substring(item.lastIndexOf('/')+1));
            return convertView;
        }

        class ViewHolder{
            TextView info;
            ViewHolder(TextView mInfo){
                info =mInfo;
            }
        }
    }
}
