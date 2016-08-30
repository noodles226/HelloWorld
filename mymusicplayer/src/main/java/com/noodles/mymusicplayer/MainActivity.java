package com.noodles.mymusicplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private Intent sendIntent;

    public static final String ACTIVITY_ENVENT = "com.noodles.mymusicplayer.ACTIVITY_EVENT";

    public static final int START_OR_PAUSE = 1;
    public static final int STOP = 0;

    private String[] authorStr = {"未知艺术家","周慧","伍佰"};
    private String[] titleStr = {"心愿","约定","伍佰"};
    private TextView author,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        author = (TextView) findViewById(R.id.author);
        title = (TextView) findViewById(R.id.title);

        
        /* 
         * 通过Context的findViewById()方法，传入系统为控件自动生成的ID（R.java文件，包含一个静态内部类ID下的静态常量，为在xml中定义的android:id）
         * 获取UI界面中控件对象
         */
        // 开始/暂停按钮对象
        Button start = (Button) findViewById(R.id.start_or_pause);
        //停止按钮
        Button stop = (Button) findViewById(R.id.stop);
        
        //注册事件
        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        //建立活动端的广播发送,指定为全局变量，以便于在按钮事件中操作
        sendIntent = new Intent();
        sendIntent.setAction(ACTIVITY_ENVENT);

        //启动一个活动，在后台运行音乐
        Intent serviceIntent = new Intent(this,MusicService.class);
        startService(serviceIntent);

        ActivityReceiver receiver = new ActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.UPDATE_ACTION);
        registerReceiver(receiver,filter);

    }

    @Override
    public void onClick(View view) {
        // 按钮点击后回调OnClickListener接口实现子类的onClick()方法，系统将自动传入当前控件的对象

        /*
         * 需要把前台按钮的点击事件，按照某种规则传递给服务，因为startService()方式中的service与activity
         * 不能进行相互通讯，所以这里借助广播的全局性特点，通过两组（每组分别包括一个发送广播和一个接收器）
         * 广播组件来间接实现服务和活动的通讯
         */
        switch (view.getId()){
            //可以利用Intent携带数据的方式，以标识不同按钮的点击
            case R.id.start_or_pause:
                sendIntent.putExtra("control",START_OR_PAUSE);
                //发送广播
                sendBroadcast(sendIntent);
                break;
            case R.id.stop :
                sendIntent.putExtra("control",STOP);
                //发送广播
                sendBroadcast(sendIntent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getIntExtra("current",-1);
            author.setText(authorStr[current]);
            title.setText(titleStr[current]);

        }
    }
}
