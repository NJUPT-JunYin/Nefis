#include<stdio.h>
#include"gf_algebra.h"

#include<time.h>


void main()
{
	int i;

	GFType a = 50, b = 55;
	GFType c,d;

	clock_t time1;//统计时间
	double t;

	int m=8;
	int n=1<<m;
	int max=1<<23;

	gf_init(m, 0);

	//算法函数效率测试

	for(i=0;i<max;i++)
	{
		c=gf_mul(n-2,n-3);
		d=gf_div(n-2,n-3);
	}

	time1=clock();

	t=((double)(time1))/CLK_TCK;

	gf_uninit();

	printf("%fs\n",t);
}
 