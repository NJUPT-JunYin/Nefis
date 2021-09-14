package com.receive.decode;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

import nc.NCUtils;
import static java.lang.Thread.sleep;

public class ClientTextureView extends TextureView implements  TextureView.SurfaceTextureListener{
    private static  final String MIME_TYPE = "video/avc";
    private static final String TAG = "ClientTextureView" ;
    private MediaCodec decode;
    private byte[] h264Buffer = new byte[80000];
    private int h264Len = 0;
    private int h264length = 0;
    private byte[] rtpData =new byte[1500];
    private byte[] h264=new byte[80000];
    //private byte[] fuben=new byte[50000];
    //private byte[] resr=new byte[3000];
    private byte[] res=new byte[1500];
    //private byte[] res2=new byte[1500];
    //private byte[] ren=new byte[200];
    long presentationTimeUs = 0;
    private volatile long sumlength=0;
//    private int flagn=101;
//    private int c=0;
//    private boolean flag=false;
    private boolean flagt =true;
    private volatile long starttime = 0;
    long chalen=0;
    long sumt = 0;
    private DatagramSocket socket;
    private DatagramSocket mSocket;
    private DatagramSocket socket1;
    private String ipa="192.168.43.255";
    private String ipb="192.168.43.1";
    private InetAddress mInetAddress;
    private InetAddress mInetAddress1;
    private int mPort=5004;
    private int mPort1=5005;
    int cc=0;
    int ccz=0;
    int count1=0;
    int k=0;
    int n=-1;
    int fu_header_len = 12;         // FU-Header长度为12字节
    byte[] temp1=new byte[3000];
    byte[] temp2=new byte[3000];
    private byte[] result=new byte[80000];
    private boolean threadflag = true;
    private Thread threadback;
    private Thread thread2;
    byte[] statement = new byte[4];
    private byte[] piecedata = new byte[10];
    private int ipflag;
    private int sumzhen=0;
    private int currentz=0;
    private int sump=0;
    private int currentp=0;
    private int sumf=1;
    private int frate;
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    ExecutorService fixThreadPool = Executors.newFixedThreadPool(30 );
    ExecutorService cacheThreadPool = Executors.newCachedThreadPool();

//    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testdianbo.h264";
//    private BufferedOutputStream outputStream;

    Runnable task =new Runnable(){
        public void run(){
            if (h264Buffer != null) {
                offerDecoder(h264Buffer,h264Len);
                h264Buffer = null;
            }
        }
    };
    Runnable task1 =new Runnable(){
        public void run(){
//            try {
//                outputStream.write(result,0,h264Len);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            offerDecoder(result,h264length);
        }
    };
    public ClientTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
//        createfile();
        try {
            socket = new DatagramSocket(5004);//端口号
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            socket1 = new DatagramSocket(5001);//端口号
            Log.e("1111111111111111", "5001端口已经开启！！");
            socket1.setReuseAddress(true);
            socket1.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            mInetAddress = InetAddress.getByName(ipa);
            mInetAddress1 = InetAddress.getByName(ipb);
            mSocket = new DatagramSocket();
            mSocket.setBroadcast(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //threadback = new Mythread1();
        //threadback.start();
        thread2 = new Mythread2();
        thread2.start();

    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        new PreviewThread(new Surface(surface),800,480);//手机的分辨率
    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (socket != null){
            socket.close();
            socket = null;
        }
        threadflag = false;
//        try {
//            outputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
    private  class  PreviewThread extends  Thread {
        DatagramPacket datagramPacket = null;
        public PreviewThread(Surface surface, int width , int height){
            try{
                decode = MediaCodec.createDecoderByType(MIME_TYPE);
            }catch(IOException e){
                e.printStackTrace();
            }
            final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE,width,height);
            //format.setInteger(MediaFormat.KEY_BIT_RATE,  5*width*height);
            //format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            //format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
//            byte[] header_sps = {0,0,1,103,66,(byte) 0x80,30, (byte) 0xda,3,32, (byte)0xf6, (byte)0x80,109,10,19,80};
//            byte[] header_pps = {0, 0, 0, 1, 104, (byte) 0xce, 6, (byte)0xe2};
//            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
//            try {
//                outputStream.write(header_sps,0,header_sps.length);
//                outputStream.write(header_pps,0,header_pps.length);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            decode.configure(format,surface,null,0);
            decode.start();
            start();
        }
        @Override
        public void run() {
            while (true) {
                byte[] data = new byte[1500];
                if (socket != null) {
                    try {
                        datagramPacket = new DatagramPacket(data,data.length);
                        socket.receive(datagramPacket);//接收数据
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(flagt)
                {
                    if(data!=null) {
                        long chatime=starttime;
                        starttime = System.currentTimeMillis();//设置一个开始时间点
                        Log.e( "11111开始下载时间", starttime-chatime+"ms");
                        Log.e( "1111111已下载长度",  sumlength-chalen+"B");
                        chalen=sumlength;
                        flagt=false;//只获取一次  每次发送分片信息的时候还可以获取一次
                    }
                }
                rtpData = datagramPacket.getData();
                sumlength+=data.length;
                // 在这里将长度累加 计算长度分片长度 分片计算以源节点发送分片信息为准
                //还要计算时间
//                starttime=System.currentTimeMillis();
                rtp2h2644(rtpData);
//                sumt+=System.currentTimeMillis()-starttime;
//                Log.e( "解码时间", sumt+"ms");
            }
        }
    }
    //解码h264数据
    private void offerDecoder(byte[] input, int length) {
        // Log.d(TAG, "offerDecoder: ");
        try {
            ByteBuffer[] inputBuffers = decode.getInputBuffers();
            int inputBufferIndex = decode.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                try{
                    inputBuffer.put(input, 0, length);
                }catch (Exception e){
                    e.printStackTrace();
                }
                long pts = computePresentationTime(presentationTimeUs);
                decode.queueInputBuffer(inputBufferIndex, 0, length,pts,0);
                presentationTimeUs++;
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = decode.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                decode.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = decode.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void rtp2h2644(byte[] rtpData) {//不进行再编码操作时使用这个
        // 解析FU-indicator
        byte indicatorType = (byte) (byteToInt(rtpData[fu_header_len]) & 0x1f); // 取出low 5 bit 则为FU-indicator type
//        Log.e("李玉龙", indicatorType+"成功接收//");
        if (indicatorType == 28||indicatorType == 29) {  // FU-A
            cc++;
            if (n == -1) {
                n = (int) rtpData[14];
                k = (int) rtpData[15];
                if(k<=0||k>30)return;
                count1 = 1;
                h264Len = k * 1400;
                System.arraycopy(rtpData, 16, temp1, 0, k);
                System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                if (count1==k) {
                    temp2 = NCUtils.InverseMatrix(temp1, k);
                    if (temp2 != null) {
                        NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                        h264length = h264Len;
                        ccz++;
                        //MainActivity.handle1(ccz, cc);
                        //offerDecoder(result,h264Len);
                        //cacheThreadPool.execute(task1);
                        singleThreadExecutor.execute(task1);
                        //fixThreadPool.execute(task1);
                    }
                }
            } else {
                int c =(int) rtpData[14];
                if (n == c) {
                    if (count1 >=k) {
                        return;
                    }
                    else {
                        count1++;
                        System.arraycopy(rtpData, 16, temp1,(count1-1)*k,k);
                        System.arraycopy(rtpData, 16 + k, h264, (count1 - 1) * 1400, 1400); // 负载数据
                        if(count1==k) {
                            temp2 = NCUtils.InverseMatrix(temp1, k);
                            if (temp2 != null) {
                                NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                                h264length = h264Len;
                                ccz++;
                                frate = ccz*100/sumf;
                                MainActivity.handle1(frate, cc);
                                //offerDecoder(result, h264Len);
                                //cacheThreadPool.execute(task1);
                                singleThreadExecutor.execute(task1);
                                //fixThreadPool.execute(task1);
                            }
                        }
                    }
                }
                else {
                    if(c<n&&n!=99)return;
                    n = c;
                    sumf++;
                    if(n == 0) {
                        currentz = ccz - sumzhen;
                        sumzhen+=currentz;
                        currentp =cc-sump;
                        sump+=currentp;
                        sendPacket3();
                    }
                    k = (int)rtpData[15];
                    if(k<=0||k>30)
                    {
                        n=-1;
                        return;
                    }
                    count1 = 1;
                    h264Len = k * 1400;
                    System.arraycopy(rtpData, 16, temp1, 0, k);
                    System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                    if (count1==k) {
                        temp2 = NCUtils.InverseMatrix(temp1, k);
                        if (temp2 != null) {
                            NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                            h264length = h264Len;
                            ccz++;
                            //MainActivity.handle1(ccz, cc);
                            //offerDecoder(result,h264Len);
                            //cacheThreadPool.execute(task1);
                            singleThreadExecutor.execute(task1);
                            //fixThreadPool.execute(task1);
                            }
                        }
                    }
                }
            }

        }


    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 /25;
    }

    public void sendPacket(final byte[] data,final int offset, final int size) {
        try{
            DatagramPacket p;
            p = new DatagramPacket(data, offset, size, mInetAddress, mPort);
            mSocket.send(p);
//            Log.e("李玉龙","成功发送//");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendPacket2(final byte[] data,final int offset, final int size) {
        try{//这里用来作为反馈发送的一方
            DatagramPacket p;
            p = new DatagramPacket(data, offset, size, mInetAddress1, mPort1);
            mSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendPacket3() {
        try{//反馈 解码率 和 丢包率
            ipflag = Counts.getInstance().getIpflag();
            byte[] infomation = new byte[20];
            infomation[0]=(byte)ipflag;
            //Log.e("1111111111111111","本终端ip信息标志："+ipflag);
            infomation[1]=(byte)currentz;
            //Log.e("1111111111111111","当前解码率："+currentz);
            int a = currentp/100;
            int b = (currentp-a*100)/10;
            int c = currentp%10;
            infomation[2]=(byte)a;
            infomation[3]=(byte)b;
            infomation[4]=(byte)c;
            DatagramPacket p;
            p = new DatagramPacket(infomation, 0, 20, mInetAddress1, mPort1);//5005端口
            mSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
           // Log.e("1111111111111111","向源节点发送数据失败了//");
        }
    }

    public static byte[] toByteArray(int source, int len) {
        byte[] target = new byte[len];
        for (int i = 0; i < 4 && i < len; i++) {
            target[i] = (byte) (source >> 8 * i & 0xff);
        }
        return target;
    }
    /**
     * 数组byte转int数据
     * @param source
     * @return
     */
    public static int toInt(byte[] source) {
        int target = 0;
        for (int i = 0; i < source.length; i++) {
            target += (source[i] & 0xff) << 8 * i;
        }
        return target;
    }

    //这里用来接收的当前分片的信息  以便于在屏幕上显示 出来
    class Mythread2 extends Thread
    {
        DatagramPacket datagramPacket = null;
        @Override
        public void run() {
            //需要在子线程中处理的逻辑
            //接收请求的线程应该是一直打开的
            //每接收到一个请求 就要去创建一个线程
            while (true) {
                //Log.e("1111111111111111", "准备接收数据");
                byte[] data = new byte[10];
                if (socket1 != null) {
                    flagt=true;
                    try {
                        datagramPacket = new DatagramPacket(data, data.length);
                        socket1.receive(datagramPacket);//接收数据  5001
                        //Log.e("1111111111111111", "已经接收到了数据");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    piecedata = datagramPacket.getData();

                    int i,j,k,l,m,x;
                    i = (int)piecedata[0];//分片顺序
                    j = (int)piecedata[1];//分辨率
                    k = (int)piecedata[2];//冗余、
//                    l = (int)piecedata[3];// /100的倍数
//                    m = (int)piecedata[4];// 10的余数
//                    x = (int)piecedata[5];// 10的余数
//                    l = l*100;
//                    l += m*10;
//                    l += x;
                    Counts.getInstance().setNum1(i);
                    Counts.getInstance().setNum2(j);
                    Counts.getInstance().setNum3(k);
//                    Counts.getInstance().setFramesnum(l);
                    }
                }
            }
        }

//    private void createfile(){
//        File file = new File(path);
//        if(file.exists()){
//            file.delete();
//        }
//        try {
//            outputStream = new BufferedOutputStream(new FileOutputStream(file));
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }

}
