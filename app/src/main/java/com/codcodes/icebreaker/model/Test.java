package com.codcodes.icebreaker.model;

/**
 * Created by Casper on 2016/08/05.
 */
public class Test implements IJsonable
{
    private String var;

    @Override
    public void setVarValue(String var, String value)
    {
        System.err.println("Test.class>>" + var + ":" + value);
        this.var = value;
    }

    public String getVar(){return this.var;}
}
