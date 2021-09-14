package com.sona.test;

import android.app.Application;

public class Counts extends Application {
    private static Counts instance = null;

    private int count =0;//自己定义的变量，下面生成了其get和set方法
   // private int losspagrate =0;
   // private int lossfrarate =0;
   private String px = "800480";
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

    public void setPx(String a)
    {
        this.px=a;
    }
    public String getPx()
    {
        return  px;
    }
}
