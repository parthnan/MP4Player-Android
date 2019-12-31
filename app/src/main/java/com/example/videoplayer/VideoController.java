package com.example.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;

import static com.example.videoplayer.MainActivity.VIDEO_ACTIVITY_INTENT;

public class VideoController extends MediaController {
    Context context;
    public VideoController(Context context){
        super(context);
        this.context = context;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)this.getLayoutParams();
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float dp = 50f;
        float fpixels = metrics.density*dp;  //adjusting for various pixel densities
        int pixels = (int)(fpixels+0.5f);
        params.bottomMargin = pixels;  //same
        //params.rightMargin = pixels;
        this.setLayoutParams(params);
    }

    @Override
    public void setAnchorView(View view){
        super.setAnchorView(view);
        ImageView exitButton = new ImageButton(context) ;
        exitButton.setImageDrawable(getResources().getDrawable((R.drawable.exit)));
        exitButton.setBackgroundColor(getResources().getColor(R.color.black));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        addView(exitButton,params);
        exitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(VIDEO_ACTIVITY_INTENT,"showContinueWatching");
                context.startActivities(new Intent[]{intent});
            }
        });

    }
}
