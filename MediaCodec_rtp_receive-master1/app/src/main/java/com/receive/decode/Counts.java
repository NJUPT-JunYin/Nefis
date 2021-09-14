package com.receive.decode;

import android.app.Application;

public class Counts extends Application {
    private static Counts instance = null;
    private int count =0;//自己定义的变量，下面生成了其get和set方法
    private int num1 =1;
    private int num2 =1;
    private int num3 =2;
    private int ipflag =0;
    private int framesnum =0;
    public static synchronized Counts getInstance()

    {

        if(null == instance){

            instance = new Counts();
        }
        return instance;
    }
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }
    public void setCount(int a)
    {
        this.count=a;
    }
    public int getCount()
    {
        return  count;
    }

    public void setNum1(int b)
    {
        this.num1=b;
    }
    public int getNum1(){return num1;}

    public void setNum2(int b)
    {
        this.num2=b;
    }
    public int getNum2(){return num2;}

    public void setNum3(int b)
    {
        this.num3=b;
    }
    public int getNum3(){return num3;}

    public void setIpflag(int b)
    {
        this.ipflag=b;
    }
    public int getIpflag(){return ipflag;}

    public void setFramesnum(int b)
    {
        this.framesnum=b;
    }
    public int getFramesnum(){return framesnum;}
}
