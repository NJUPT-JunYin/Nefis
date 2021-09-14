package com.sona.readFileDecode;

import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sona.test.Counts;
import com.sona.test.NCUtils;
/**
 * Created by ZhangHao on 2017/5/5.
 * 读取H264文件送入解码器解码线程
 */
public class ReadH264FileThread extends Thread {
    //解码器
    private MediaCodecUtil util;
    //文件路径
    private String path;
    //文件读取完成标识
    private boolean isFinish = false;
    //这个值用于找到第一个帧头后，继续寻找第二个帧头，如果解码失败可以尝试缩小这个值
    private int FRAME_MIN_LEN = 1024;
    //一般H264帧大小不超过200k,如果解码失败可以尝试增大这个值
    private static int MAX_FRAME_BUF_LEN = 100 * 1024;
    //根据帧率获取的解码每帧需要休眠的时间,根据实际帧率进行操作
    private int PRE_FRAME_TIME = 1000 /15;
    private String path1 ="/test";
    private String path2 =".h264" ;
    private File file;
    private int i=1;
    private String[] resolution={"480320","800480","960720"};
    private int resflag=1;
    private Mythread1 mythread1;
    private int countframes = 0;
    private int sumpkg=0;
    private int pkg100 =0;
    private int pkg=0;
    private int ratep;
    private int ratef= 95;
    private boolean working=true;
    private DatagramSocket socket;
    private int redundance = 2;
    private byte[] frameBuffer = new byte[MAX_FRAME_BUF_LEN];
    private byte[] fileBuffer = new byte[10 * 1024];
    private float info[] = new float[15];
    private byte[] udpdata = new byte[20];
    private byte[] b = new byte[4];
    private String ipa="192.168.43.34";
    private int ipflag = 0;
    private InetAddress InetAddress;
    private DatagramSocket mSocket;
    private byte[] infodata = new byte[5];
    //设立一个整型数组用来在片段切换的时候存放上一个片段的总的包数 帧数
    /**
     * 初始化解码器
     *
     * //@param util 解码Util
     * @param path 文件路径
     */
    //回头改变路径的时候应该每次直接获取内存目录 加上 分辨率选择 以及分片序号选择
    //public ReadH264FileThread(MediaCodecUtil util, String path, String ip, int flag) {
    public ReadH264FileThread(String path, String ip, int flag) {
        //this.util = util;
        util = new MediaCodecUtil(ip);
        this.path = path ;
        this.ipa  = ip;
        this.ipflag =flag;
        try {
            InetAddress = InetAddress.getByName(ipa);
            mSocket = new DatagramSocket();
            mSocket.setBroadcast(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            socket = new DatagramSocket(5005);//端口号
            Log.e("1111111111111111", "5005端口已经开启！！");
            socket.setReuseAddress(true);
            socket.setBroadcast(false);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        mythread1=new Mythread1();
        mythread1.start();
    }

    private int findHead(byte[] data, int start, int len)
    {
        Log.d("lkb", "findHead: start="+start+" len="+len);
        int offset = start;
        int end = offset + len;
        int found = -1;
        //寻找头部 可以试下00 00 01而不是00 00 00 01
        while(offset + 3 < end){
            //if (data[offset+0]==0x0 && data[offset+1]==0x0 && data[offset+2]==0x0&& data[offset+3]==0x1){
            if (data[offset+0]==0x0 && data[offset+1]==0x0 && data[offset+2]==0x1){
                found = offset;
                break;
            }
            offset++;
        }
        return found;
    }

    @Override
    public void run() {
        super.run();
        Log.d("lkb", "run +");

        int frameDataLen; //frame buffer里已有数据偏移
        int head1,head2;
        int numRead;
        int head_id;
        int findOffset;
        boolean bNeedMoreData;
        //long dataOffsetInFile_debug; //for debug

        head1 = 0;
        head2 = 0;
        numRead = 0;
        head_id = 1; //首先寻找head1
        bNeedMoreData = true;
        frameDataLen = 0;
        findOffset = 0;
        //dataOffsetInFile_debug = 0;
        //判断文件是否存在  这个地方判断文件是不是存在  那么这里发送的视频片段结束后就立刻读取下一个片段 中间确定选择哪个分辨率
                //这里应该是由几个片段的循环 不停的取下一个片段的地址 在这个基础上要进行片段去取哪一个片段  这就是要取决于终端的带宽状况了
                //带宽由终端反馈到服务器 服务器自主的判断 具体怎么操作再想想 还有就是冗余情况 冗余的变化 也要进行 可以改变的两点 分辨率和冗余
                //第一次发送中等分辨率的视频 下一个就要进行判断了
                //虽然能发送了但是很卡 分片之间切换的不流畅
                // 这里还可以加上一个发送包 发送当前的分片信息和冗余信息
            String pathall = path + path1 + resolution[resflag] + i + path2;
            infodata[0]=(byte) resflag;
            infodata[1]=(byte) i;
            infodata[2]=(byte)redundance;
            sendPacket(infodata,0,3);
            Log.e("1111111111111111111", "当前选择信息已经发送");
            file = new File(pathall);
            if (file.exists()) {
                //Log.d("lkb", "file exist" + file.getAbsolutePath());
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    long startTime = System.currentTimeMillis();
                    while (true) {
                        if (bNeedMoreData) {
                            if (fileInputStream.available() > 0) {
                                numRead = fileInputStream.read(fileBuffer);
                                Log.d("lkb", "read data len=" + numRead);
                            } else {
                                //应该将上个分片的东西清空
                                Log.d("lkb", "file end");//这里是一个文件读完的地方
                                i++;
                                if(i>=9) {
                                    //没有分片时
                                    util.stopCodec();
                                    working = false;
                                    //这里没有分片时 结束
                                    break;
                                }
                                //就在这里切换分片
                                if(ratef > 98)
                                {
                                    if(resflag<2) {
                                        resflag++;
                                    }
                                }
                                if(ratef <80)
                                {
                                    if(resflag>0) {
                                        resflag--;
                                    }
                                }
                                // 选择要发送的信息  可以加上已发送的数据帧数
                                infodata[0]=(byte)resflag;
                                infodata[1]=(byte)i;
                                infodata[2]=(byte)redundance;
                                sendPacket(infodata,0,3);
                                pathall = path + path1 + resolution[resflag] + i + path2;
                                file = new File(pathall);
                                fileInputStream = new FileInputStream(file);
                                numRead = fileInputStream.read(fileBuffer);
                                frameDataLen=0;
                                head_id=1;
                            }
                            bNeedMoreData = false;
                        }
                        switch (head_id) {
                            case 1: {
                                head1 = findHead(fileBuffer, 0, numRead);
                                if (head1 != -1) { //找到head1
                                    head_id = 2; //寻找head2
                                } else { //找不到head1
                                    bNeedMoreData = true; //再读文件
                                    //dataOffsetInFile_debug += numRead;
                                }
                                break;
                            }
                            case 2: {
                                head2 = findHead(fileBuffer, head1 + findOffset, numRead - head1 - findOffset);
                                if (head2 != -1) { //找到head2
                                    System.arraycopy(fileBuffer, head1, frameBuffer, frameDataLen, head2 - head1);
                                    frameDataLen += head2 - head1;
                                    onFrame(frameBuffer, 0, frameDataLen); //得到一帧
                                    frameDataLen = 0;
                                    head1 = head2; //将此次head2作为下次head1，再此寻找head2
                                    findOffset = 4;
                                    //线程休眠
                                    sleepThread(startTime, System.currentTimeMillis());
                                    //重置开始时间
                                    startTime = System.currentTimeMillis();
                                } else { //只找到head1没有找到head2，保存此次数据到frameBuffer
                                    System.arraycopy(fileBuffer, head1, frameBuffer, frameDataLen, numRead - head1);
                                    frameDataLen += numRead - head1; //增加frame buffer 已有数据偏移
                                    head1 = 0; //重新读文件后，寻找head2是从偏移0(head1+4)开始
                                    findOffset = 0;
                                    bNeedMoreData = true; //再读文件
                                    //dataOffsetInFile_debug += numRead;
                                }
                                break;
                            }
                        }
                    }//while(1)
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ReadH264FileThread", "File not found");
            }
        Log.d("lkb", "run -");
    }

    //视频解码
    private void onFrame(byte[] frame, int offset, int length) {
        Log.d("lkb", "onFrame "+frame[0]+" "+frame[1]+" "+frame[2]+" "+frame[3]);
        if (util != null) {
            try {
                if(length%1400==0)
                {pkg=length/1400;}
                else
                {
                    pkg=length/1400+1;
                }
                sumpkg+=pkg;
                util.SendThread(frame,length,redundance,pkg,countframes);
                countframes++;
//                if(countframes%100 == 0)
//                {
//                    pkg100=sumpkg;
//                    sumpkg =0;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("MediaCodecRunnable", "mediaCodecUtil is NULL");
        }
    }

    //修眠
    private void sleepThread(long startTime, long endTime) {
        //根据读文件和解码耗时，计算需要休眠的时间
        //long s=System.currentTimeMillis() - startTime;
        long time = PRE_FRAME_TIME - (endTime - startTime);
        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //手动终止读取文件，结束线程
    public void stopThread() {
        isFinish = true;
    }

    public static int toInt(byte[] source) {
        int target = 0;
        for (int i = 0; i < source.length; i++) {
            target += (source[i] & 0xff) << 8 * i;
        }
        return target;
    }

    class Mythread1 extends Thread
    {
        DatagramPacket datagramPacket = null;
        @Override
        public void run() {
            while (working) {
                byte[] data = new byte[20];
                if (socket != null) {
                    try {
                        datagramPacket = new DatagramPacket(data, data.length);
                        socket.receive(datagramPacket);//接收数据
                        //Log.e("1111111111111111", "已经接收到了数据");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    udpdata = datagramPacket.getData();
                    int d = (int)udpdata[0];
                    Log.e("111readh264file", "第一个字节:" + d);
                    if(d==ipflag) {
                        Log.e("1111111111111111", "标志："+ipflag);
                        ratef = (int)udpdata[1];
                        int a = (int)udpdata[2];
                        int b = (int)udpdata[3];
                        int c = (int)udpdata[4];
                        int s =a*100+b*10+c;
                        Log.e("1111111111111111", "标志 s："+s);
                        //ratep = s*100/pkg100;
                        if(ratef > 96&&redundance>1)
                        {
                            redundance--;
                            Log.e("1111111111111111", "标志减少redundance："+ redundance);
                        }
                        if(ratef < 90&& redundance<4)
                        {
                            redundance++;
                            Log.e("1111111111111111", "标志增加redundance："+ redundance);
                        }
                    }
                }
            }
        }
    }

    public void sendPacket(final byte[] data,final int offset, final int size) {
        try{
            DatagramPacket p;
            p = new DatagramPacket(data, offset, size, InetAddress, 5001);
            //Log.e("111111111111111", "已经向5001端口发送本分片的信息了");
            //可以在发送分片信息的时候 顺便将已经发送的帧数已经数据包的数量 发送过去
            mSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
