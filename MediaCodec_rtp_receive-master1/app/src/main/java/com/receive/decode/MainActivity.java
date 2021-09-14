package com.receive.decode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import nc.*;

public class MainActivity extends Activity {
    private String[] res = {"480*320","800*480","960*720"};
    private DatagramSocket mSocket;
    private String ip="192.168.43.1";
    private InetAddress mInetAddress;
    private int mPort=5006;
    Button but;
    public static Handler handler;
    TextView textView;
    private String ipv4;
    private byte[] ipbyte;
    private int ipflag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        but=(Button)findViewById(R.id.count);
        init();
        handler =new MyHandler2();
        textView=(TextView)findViewById(R.id.textView);
        but = (Button)findViewById(R.id.button1);
        ipv4 = getIP(this);
        Log.e("李玉龙11111111111111","当前获取的上网IP："+ipv4);
        String s =ipv4.substring(11,ipv4.length());//争取
        ipflag = Integer.parseInt(s);
        Counts.getInstance().setIpflag(ipflag);//将IP的标志放入全局变量 以便于使用
        Log.e("李玉龙11111111111111","当前获取的上网IP最后一个地址："+s);
        Log.e("李玉龙11111111111111","当前获取的上网IP最后一个地址的数字表示："+ipflag);
        //Log.e("李玉龙11111111111111","当前获取的上网IP："+ipv4);
        //成功获取当前局域网内的IPv4地址
        //将这个地址和请求一起发送给 服务器  服务器根据请求的地址创建 线程
        ipbyte = ipv4.getBytes();
        // String sip = new String(ipbyte);
        //Log.e("李玉龙11111111111111","当前数组长度"+ipbyte.length);
        //Log.e("李玉龙11111111111111","当前获取的上网IP："+sip);
        // 系统自带的
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //这里的话发送一个请求然后  服务器发送给终端视频
                //这里是在开一端口 还是怎么发送呢？
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] b = new byte[20];
                            int k = 0;//请求开始标志K
                            int iplength = ipbyte.length;
                            int k1 = 110;//请求结束标志
                            //这里请求中加上IP地址 以及其长度
                            b[0]=(byte)k;
                            b[1]=(byte)ipflag;
                            b[2]=(byte)iplength;
                            System.arraycopy(ipbyte,0,b,3,iplength);
                            int len = iplength+3;
//                            byte[] temp = new byte[iplength];
//                            System.arraycopy(b,3,temp,0,iplength);
//                            String s1 = new String(temp);
//                            Log.e("李玉龙11111111111111","当前获取的上网IP地址："+s1);
                            b[len]=(byte)k1;
                            //数据报的构成 请求头 标志位 IP地址长度 IP地址 请求尾
                            sendPacket(b,0,len+1);
                            sendPacket(b,0,len+1);
                            //sendPacket(b,0,2);
                            //Log.e("李玉龙11111111111111","成功发送//");
                            Log.e("李玉龙11111111111111","成功发送//");
                            Log.e("李玉龙11111111111111","成功发送//");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
//                if (mSocket != null){
//                    mSocket.close();
//                    mSocket = null;
//                }
            }
        });
//        but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int a=Counts.getInstance().getCount();
//                int b=Counts.getInstance().getCountz();
//                Toast.makeText(MainActivity.this,"收包数："+a+"|解帧数："+b,Toast.LENGTH_LONG).show();
//            }
//        });
//        byte[] a={1,2,3,4};
//        byte[] b={2,4,6,8};
//        byte[] b=new byte[4];
//        byte[] c=new  byte[4];
//        c=NCUtils.mul(a,b);
//        int i=0;
//        for(i=0;i<4;i++)
//        {
//            Log.e("李玉龙",(int)c[i]+"//");
//        }
//        int c=NCUtils.getRank1(b,2,2);
//        Log.e("李玉龙",c+"//");
        Log.e("李玉龙11111111111111","这里开始测试异或运算的正确性//");
        //异或运算已经成功了
//        byte[] a=new byte[3];
//        byte[] b={2,4,6,8,2,5,1,3,7};
//        byte[] c=new  byte[2];
//        c=NCUtils.test(a,b);
//        int i=0;
//        for(i=0;i<3;i++)
//       {
//           Log.e("李玉龙",(int)c[i]+"//");
//        }
    }

    public void  init()
    {
        try {
            mInetAddress = InetAddress.getByName(ip);
            mSocket = new DatagramSocket();
            mSocket.setBroadcast(true);
            Log.e("李玉龙11111111111111","成功初始化//");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendPacket(final byte[] data,final int offset, final int size) {
        try{
            DatagramPacket p;
            p = new DatagramPacket(data, offset, size, mInetAddress, mPort);//5006 端口
            mSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIP(Context context){

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }


    public static void handle1(int a,int b)
    {
        Message message = Message.obtain();
        message.what=0;
        message.arg1 = a;
        message.arg2 = b;
        handler.sendMessage(message);
    }
    class MyHandler2 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            int a = msg.arg1;
            int b = msg.arg2;
            float c=a*100/875;
            String name = " DecodeRate:";
//            String name1 = "接收包数：";
            String name2 = " rate:";
            String name3 = "no.";
            String name4 = " px:";
            String name5 = " K:";
//            String name6 = " Sframes:";
//            String name3 = "丢包率：";
            int i = Counts.getInstance().getNum1();
            int j = Counts.getInstance().getNum2();
            int k = Counts.getInstance().getNum3();
            switch (what) {
                case 0:
                    textView.setText(name3+j+name4+res[i]+name5+k+name + a );
                    //textView.setText(name3+j+name4+res[i]+name5+k+name + a +name2+c+"%");
//                    textView.setText(name + a + name1 + b+name2+c+"%"+name3+d+"%");
                    break;
                default:
                    break;
            }
        }
    }
}
