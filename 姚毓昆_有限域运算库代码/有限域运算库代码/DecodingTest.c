#include<stdio.h>
#include"gf_algebra.h"

#include<time.h>


void main()
{
	byte** encodeMatrix;//随机编码矩阵
	byte** Matrix;//数据矩阵
	byte** Mat;//存储编码结果
	byte** IvEncodeMatrix;//逆矩阵
	byte** Mat0;//原数据

	int i,j,h,q;

	GFType a = 50, b = 55;
	GFType c,d;

	clock_t time1,time2;//统计时间
	double t,t1;

	int m=8;
	int n=1<<m;
	int max=1<<23;

	int row=10;//随机编码矩阵的大小
	int col=10000000;//x1大小
	int k=10;

	int temp;
	double p;

	gf_init(m, 0);


	//生成随机矩阵///////////////////////////
	encodeMatrix= (byte**)malloc(sizeof(byte)*(row*4));
	for(i=0;i<row;i++)
	{
		encodeMatrix[i] = (byte*)malloc(sizeof(byte)*(k*4));
	}
	srand((unsigned)time(NULL));
	for(i=0;i<row;i++)
	{
		for(j=0;j<8;j++)
		{
			encodeMatrix[i][j]=rand()%256;
//			printf("%d   ",encodeMatrix[i][j]);
		}
		for(j=8;j<k;j++)
		{
			encodeMatrix[i][j]=0;
//			printf("%d   ",encodeMatrix[i][j]);
		}
	}
	//待编码矩阵//////////////////////////////
	Matrix= (byte**)malloc(sizeof(byte)*(k*4));
	for(i=0;i<k;i++)
	{
		Matrix[i] = (byte*)malloc(sizeof(byte)*(col*4));
	}
	srand((unsigned)time(NULL));
	for(i=0;i<k;i++)
	{
		for(j=0;j<col;j++)
		{
			Matrix[i][j]=rand()%256;
//			printf("%d   ",Matrix[i][j]);
		}
	}

	////////////////////////////////////////////编码过程开始
	///////////////////编码输出Mat
	Mat= (byte**)malloc(sizeof(byte)*(row*4));
	for(i=0;i<row;i++)
	{
		Mat[i]=(byte*)malloc(sizeof(byte)*(col*4));
	}
	for(i=0;i<row;i++)
	{
		for(j=0;j<8;j++)
		{
			temp=0;
			for(h=0;h<k;h++)
			{
				temp=gf_add(temp,gf_mul(encodeMatrix[i][h],Matrix[h][j]));//编码
			}
			Mat[i][j]=temp;
	//		printf("%d  ",Mat[i][j]);
		}
		for(j=8;j<col;j++)
		{
			Mat[i][j]=0;
	//		printf("%d  ",Mat[i][j]);
		}
	}
	///////////////////////////////////////////编码过程结束

	time1=clock();
	t1=(double)(time1)/CLK_TCK;

	/////////////////////////////////////////解码过程开始
	IvEncodeMatrix= (byte**)malloc(sizeof(byte)*(row*4));
	for(i=0;i<row;i++)
	{
		IvEncodeMatrix[i] = (byte*)malloc(sizeof(byte)*(k*4));
	}

	//矩阵求逆
	//step1:从上向下
	for ( i = 0; i < k; i++)
	{
		for ( j = i + 1; j < k; j++)//更新一次矩阵
		{
			p=encodeMatrix[j][i] / encodeMatrix[i][i];
			for ( h = 0; h < k; h++)//更新第j行
			{
				encodeMatrix[j][h] = encodeMatrix[j][h] - encodeMatrix[i][h] * p;
				IvEncodeMatrix[j][h] = IvEncodeMatrix[j][h] - IvEncodeMatrix[i][h] * p;
			}
		}
	}
	//step2:从下向上
	for ( i = k - 1; i >= 0; i--)
	{
		for ( j = i - 1; j >= 0; j--)//更新一次矩阵
		{
			p=encodeMatrix[j][i] / encodeMatrix[i][i];
			for ( h = 0; h < k; h++)//更新第j行
			{
				encodeMatrix[j][h] = encodeMatrix[j][h] - encodeMatrix[i][h] * p;
				IvEncodeMatrix[j][h] = IvEncodeMatrix[j][h] - IvEncodeMatrix[i][h] * p;
			}
		}
	}
	//step3:单位化
	for ( i = 0; i < k; i++)
	{
		p = encodeMatrix[i][i];
		for ( j = 0; j < k; j++)
		{
			IvEncodeMatrix[i][j] = IvEncodeMatrix[i][j] / p;
			encodeMatrix[i][j] = encodeMatrix[i][j] / p;
		}
	}
     

	//矩阵相乘///////////////////////////////

	Mat0= (byte**)malloc(sizeof(byte)*(k*4));
	for(i=0;i<k;i++)
	{
		Mat0[i] = (byte*)malloc(sizeof(byte)*(col*4));
	}
	for(i=0;i<row;i++)
	{
		for(j=0;j<col;j++)
		{
			temp=0;
			for(h=0;h<k;h++)
			{
				temp=gf_add(temp,gf_mul(IvEncodeMatrix[i][h],Mat[h][j]));//解码
			}
			Mat0[i][j]=temp;
	//		printf("%d  ",Mat[i][j]);
		}
	}
	/////////////////////////////////////////解码过程结束

	time2=clock();

	t=((double)(time2 - time1))/CLK_TCK;

	gf_uninit();

	printf("%fs\n",t);
}
 