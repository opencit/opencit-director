/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.service.impl;


/**
 *
 * @author GS-0681
 */
public class AutowireTest {

    public static void main(String[] args) {
    	test();
    }
    
    private static void test() throws ArithmeticException{
    	String s = null;
    	try{
    		throw new ArithmeticException();
    	}catch(ArithmeticException e){
    		throw e;
    	}finally{
    		System.out.println("***************");
    	}

    }
}
