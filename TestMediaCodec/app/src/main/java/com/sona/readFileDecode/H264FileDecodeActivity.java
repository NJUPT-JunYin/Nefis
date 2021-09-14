package com.sona.readFileDecode;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sona.test.Counts;
import com.sona.test.R;
import com.sona.test.NCUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class H264FileDecodeActivity extends AppCompatActivity {
    //SurfaceView
    private SurfaceView playSurface;
    private SurfaceHolder holder;
    //解码器
    public static Handler handler;
    private MediaCodecUtil codecUtil;
    //读取文件解码线程
    private ReadH264FileThread thread;
    private DatagramSocket socket1;
    private byte[] reqdata = new byte[20];
    private boolean flag = true;
    private boolean firstopen = true;
    private Mythread2 mythread2;
    //文件路径
    //private String path = Environment.getExternalStorageDirectory().toString() + "/test.h264";//点播的话路径要实时更新 要等手机传过来 单播 一个手机建立一个线程传输
    private String path = Environment.getExternalStorageDirectory().toString() ;
    Button but;
    TextView textView;
    private int[] ipflag = new int[10];
    private int countip = 0;
    private boolean iptrue =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h264_file_decodec);
        but=(Button)findViewById(R.id.zhanshi);
        handler =new MyHandler2();
        textView=(TextView)findViewById(R.id.textView);
        Log.e("1111111111111111", "正在初始化！！！！！");
        try {
            Log.e("1111111111111111", "正在初始化端口！！！！！");
            socket1 = new DatagramSocket(5006);//端口号
            Log.e("1111111111111111", "5006端口已经开启！！");
            socket1.setReuseAddress(true);
            socket1.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = Counts.getInstance().getCount();
                Toast.makeText(H264FileDecodeActivity.this,""+c,Toast.LENGTH_SHORT).show();
            }
        });
        //initSurface();
    }
//    private void initSurface() {
//        playSurface = (SurfaceView) findViewById(R.id.play_surface);
//        holder = playSurface.getHolder();
//        holder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//            }
//
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                Log.d("lkb", "surface created");
//                if (codecUtil == null) {
//                    codecUtil = new MediaCodecUtil(holder);
//                    Log.d("lkb", "start codec");
//                    codecUtil.startCodec();
//                }
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                Log.d("lkb", "surface destroy");
//                if (codecUtil != null) {
//                    Log.d("lkb", "stop codec");
//                    codecUtil.stopCodec();
//                    codecUtil = null;
//                }
//            }
//        });
//    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                // 这里是创建读取文件线程的地方 应该先创建一个接受线程 在接受线程里面相应 终端的请求
                //接收到请求后再去传输视频文件
                if(firstopen) {
                    Log.e("1111111111111111", "准备开启线程");
                    mythread2 = new Mythread2();
                    mythread2.start();
                    Log.e("1111111111111111", "开启线程");
                    firstopen = false;
                    Toast.makeText(H264FileDecodeActivity.this,"服务器已经开始接受请求！",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.e("1111111111111111", "线程已经开启，无需重复启动");
                }
//                if (thread == null) {
//                    thread = new ReadH264FileThread(codecUtil, path);
//                    thread.start();
//                }
//                break;
        }
    }
    public static void handle1(int a,int b,int c)
    {
        //将要用ipflag来区分一下 怎么显示 将具体的值赋值给message.what  还是要显示最终的发送数量 否则不好计算比率
        Message message = Message.obtain();
        message.what=0;
        message.arg1 = a;
        message.arg2 = b;
        message.obj = c;
        handler.sendMessage(message);
    }
    class MyHandler2 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //多个线程进行 不一致时要考虑一下  根据what判断
            int what = msg.what;
            int a = msg.arg1;
            int b = msg.arg2;
            int c = (int)msg.obj;
            String name = "帧数：";
            String name1 = " 包数：";
            String name2 = " 冗余：";
            String name3 = " 分辨率：";
            String reslu = Counts.getInstance().getPx();
            switch (what) {
                case 0:
                    textView.setText(name + a + name1 + b+name2+c + name3 + reslu);
                    break;
                default:
                    break;
            }
        }
    }

    class Mythread2 extends Thread
    {
        DatagramPacket datagramPacket = null;
        @Override
        public void run() {
            //需要在子线程中处理的逻辑
            //接收请求的线程应该是一直打开的
            //每接收到一个请求 就要去创建一个线程
            while (flag) {
               // Log.e("1111111111111111", "准备接收数据");
                byte[] data = new byte[20];
                if (socket1 != null) {
                    try {
                        datagramPacket = new DatagramPacket(data, data.length);
                        socket1.receive(datagramPacket);//接收数据
                        Log.e("1111111111111111", "已经接收到了数据");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    reqdata = datagramPacket.getData();
                    int d = (int)reqdata[0];
                    //Log.e("1111111111111111", "已经接收到了数据d"+d);
                    int k = (int)reqdata[1];//IP 标志
                    //Log.e("1111111111111111", "已经接收到了数据k"+k);
                    int len =(int)reqdata[2];//ip地址长度
                    //Log.e("1111111111111111", "已经接收到了数据len"+len);
                    if(d == 0)
                    {
                        if(countip == 0 ){
                            ipflag[countip] = k;
                            countip++;
                            iptrue = true;
                            Log.e("1111111111111111", "正常1");
                        }
                        else
                        {
                            int i;
                            for ( i=0;i< countip;i++)
                            {
                                if(ipflag[countip] == k)
                                {
                                    break;
                                }
                            }
                            if(i >=countip)
                            {
                                ipflag[countip] = k;
                                countip++;
                                iptrue =true;
                            }
                        }
                        if(iptrue)
                        {
                            Log.e("1111111111111111", "正常4");
                            byte[] ipbyte =new byte[len];
                            System.arraycopy(reqdata,3,ipbyte,0,len);
                            String sip = new String(ipbyte);
                            thread = new ReadH264FileThread(path, sip ,k);
                            //thread = new ReadH264FileThread(codecUtil, path, sip ,k);
                            //Log.e("1111111111111111", "准备开启线程");
                            thread.start();

                        }

                        //多个终端时 不在关闭线程 要一直接受数据请求
//                        if (thread == null) {
//                            thread = new ReadH264FileThread(codecUtil, path);
//                            Log.e("1111111111111111", "准备开启线程");
//                            thread.start();
//                            }
//                            if(socket1!=null)
//                            {
//                                socket1.close();
//                                socket1 = null;
//                                Log.e("1111111111111111", "5006端口已经关闭");
//                            }
//                        flag = false;
//                        break;
                    }
                }
            }
        }
    }
}
