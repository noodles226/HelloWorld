package com.noodles.mymusicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {

    public static final String UPDATE_ACTION = "com.noodles.mymusicplayer.UPDATE_ACTION";

    //定义三种状态，分别要静态常量表示，以便活动可以调用
    public static final int READY = 0x001;
    public static final int RUNNING = 0x002;
    public static final int PAUSE = 0x003;
    //创建一个全局变量状态来记录播放器的状态，默认是ready状态。
    private int status = READY;
    private ServiceReceiver receiver;

    //创建一个字符串数组，初始化为几首歌曲的名称，固定循环读取其中的歌曲
    private String[] musics = {"one.mp3","two.mp3","three.mp3"};
    //播放器对象
    private MediaPlayer mediaPlayer;
    //本地媒体资源管理器
   private AssetManager asset;
    //用一个变量记录歌曲当前位置，默认从0开始，对应数组的角标
    private int current = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //动态注册该广播接收器
        //1.创建广播接收器对象
        receiver = new ServiceReceiver();
        //2.创建一个intentfilte对象
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTIVITY_ENVENT);
        //3.调用Context.registerReceiver()方法
        registerReceiver(receiver,filter);
//        Toast.makeText(this, "=====", Toast.LENGTH_SHORT).show();

        /*
         * 现在需要创建一个播放器从本地资源中读取歌曲，
         */
        //返回一个AssetManager对象
        asset = getAssets();
        //创建一个媒体播放器对象
        mediaPlayer = new MediaPlayer();
        //mediaPlayer播放结束后需要启动下一首歌，并且需要给活动发送歌名和歌手的信息
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //第一首歌放完，current加1，切到下一首。
                current ++;
                //如果超过数组的范围，则从第一首歌开始循环。
                if(current > 2) {
                    current = 0;
                }
                SendBroadcastToActivity();
                //准备播放音乐
                PrepareAndPlay(musics[current]);
            }
        });
    }

    private void SendBroadcastToActivity() {
        //发送广播
        Intent sendIntent = new Intent(UPDATE_ACTION);
        sendIntent.putExtra("current",current);
        sendBroadcast(sendIntent);
    }

    private void PrepareAndPlay(String music) {
        try{
            AssetFileDescriptor afd = asset.openFd(music);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
     * 我要在服务内部创建一个广播接收器，这种的好处就是：因为成员内部类持有外部类的对象，而且把广播接收器建成内部类
     * 不影响广播器去接收广播。
     */

    //创建一个广播接收器，接收活动端发送的广播
    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //根据接收到的信息，做对应的处理
            int control = intent.getIntExtra("control",-1);
            if(control == MainActivity.START_OR_PAUSE) {
                //播放器在不同状态下，点击stop/pause按钮做出不同的处理，所以就可以分析出播放器有三种状态1.准备 2.运行 3.暂停
                if(status == READY){
                    //播放器启动
                    PrepareAndPlay(musics[current]);
//            Toast.makeText(context, "=====", Toast.LENGTH_SHORT).show();
                    //把状态变为RUNING
                    status = RUNNING;
                }else if(status == RUNNING){
                    //播放器暂停
                    mediaPlayer.pause();
                    //把状态变为PAUSE
                    status = PAUSE;
//                    Toast.makeText(context, "pause"+status, Toast.LENGTH_SHORT).show();
                }else if(status == PAUSE){
//                    Toast.makeText(context, "RUNNING"+status, Toast.LENGTH_SHORT).show();
                    //播放器进行运行
                    mediaPlayer.start();
                    //状态变为RUNNING
                    status = RUNNING;
                }

            }else if(control == MainActivity.STOP){
                //如果是点击的stop，那么试着查看后台播放器是否有对应的功能
                if (status == PAUSE || status == RUNNING){
                    //播放器变为准备状态
                    mediaPlayer.stop();
                    status = READY;
                }
            }
            SendBroadcastToActivity();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销广播接收器
        unregisterReceiver(receiver);
    }
}
