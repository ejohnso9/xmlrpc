package com.divisiblebyzero.xmlrpc.model.handlers; 

import java.util.List;
import java.util.ArrayList;

public class SimpleMath 
{
    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int mul(int a, int b) {
        return a * b;
    }

    public int div(int a, int b) {
        return a / b;
    }

    public int vectorAdd(Object[] objArray)
    {
        System.out.println("vectorAdd(Integer[])");
        Integer i;
        Integer sum = 0;
        for (Object obj : objArray) {
            i = (Integer)obj;

            // DEBUG
            //System.out.print("obj = " + obj);
            //System.out.println(" [" + obj.getClass().getName() + "]");

            sum += i;
        }

        return sum;
    }

    public List listEcho(List inputList)
    {
        ArrayList outputList = new ArrayList();
        for (Object obj : inputList) {
            outputList.add(obj);

            // DEBUG
            //System.out.print("obj = " + obj);
            //System.out.println(" [" + obj.getClass().getName() + "]");
        }

        return outputList;
    }
}
