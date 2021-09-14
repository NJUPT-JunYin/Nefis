1 Introduction
A finite field library for software implementation of Network Coding——This program can efficiently carry out various operations in the process of network encoding and decoding, including addition, subtraction, multiplication, division, addition negative element, multiplication inverse element, power operation ,etc.
Coded by Yukun Yao, Jun Yin
Nanjing University of Posts and Telecommunications, MAY, 2020

2 File Structure
gf_algebra.h and gf_lookuptable.h are used to declare functions.

3 How to use it.
1) Copy the two files (xx.h and xx.c) in your project, and include them in your project.
If you choose to use arithmetic operation,include gf_algebra.h.And then if you use the original method,include OriginalMethod.c.If you use the improvement method 1,include AlgebraMethod_1.c.If you use the improvement method 2,include AlgebraMethod_2.c.
If you choose to use the two-dimensional look-up table method,include gf_lookuptable.h and gf_lookuptable.c.
2) Use the function to initial your field: gf_init(int nOrder, int nPrim) in which the first parameter is the order of Galois field, and the the second parameter is the primitive polynomial. 
For example, gf_init(8, 0x187); The Galois field GF(2^8) is created, and the primitive ploynomial is 0x11d.
3) Now, you can use the other functions such as gf_add(a,b), gf_sub(a,b), gf_mul(a,b), gf_div(a,b), gf_neg(a), gf_inv(a). 
gf_neg(a): Return a's complement element b, a+b = 0;
gf_inv(a): Return a's inverse element b, a*b =1;