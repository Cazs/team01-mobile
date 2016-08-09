package com.codcodes.icebreaker.auxilary;

import com.codcodes.icebreaker.model.IJsonable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Casper on 2016/08/05.
 */
public class JSON
{
    private String json;
    //private static IJsonable jsonableClassInstance;
    private static final boolean DEBUG = false;
    public static final String TAG = "ICEBREAK/JSON";
    private static boolean CHUNKED = false;

    public static <T extends IJsonable> ArrayList<T> getJsonableObjectsFromJson(String json, ArrayList<T> jsonables, Class<T> cls) throws IllegalAccessException, InstantiationException {
        //ArrayList<IJsonable> jsonables = new ArrayList<IJsonable>();
        //remove square brackets
        json = json.replaceAll("\\[", "");
        json = json.replaceAll("\\]", "");
        //System.out.println("Processing: " + json);
        while(json.contains("{") && json.contains("}"))
        {
            int startPos = json.indexOf("{");//next index of {
            int endPos = json.indexOf("}");//next index of }
            if(startPos>=0 && startPos<endPos)
            {
                String obj = json.substring(startPos, endPos + 1);//remove braces
                if (DEBUG) System.out.println("Jsonable>>" + obj);
                //Create instance of type T
                //Object currJsonable = new Object();
                T jsonable = cls.newInstance();//(T)currJsonable;
                //Get jsonable
                getJsonable(obj, jsonable);
                if (jsonable != null)
                    jsonables.add(jsonable);
                else if (DEBUG) System.err.println("Jsonable returned is null.");

                if (json.length() > endPos + 2)
                    json = json.substring(endPos + 2, json.length());
                else
                    break;
                if (DEBUG) System.out.println("New JSON: " + json);
            }
            else
            {
                System.err.println("Invalid JSON Object, startPos and/or endPos are invalid: start=" + startPos + ", end=" + endPos);
                //TODO: Notify user and log
            }
        }
        return jsonables;
    }

    public static <T extends IJsonable> void getJsonable(String singleJsonObj, T jsonableClassInstance)
    {
        if(DEBUG)System.out.println("Reading JSON object: " + singleJsonObj);
        //Regular expression to match key-value pairs in JSON object
        String json_regex = "\"([a-zA-Z0-9\\s~`!@#$%^&*)(_+-={}\\[\\];',./\\|<>?]*)\"\\:(\"[a-zA-Z0-9\\s~`!@#$%^&*()_+-={}\\[\\];',./\\|<>?]*\"|\"[0-9,]\"|\\d+)";
        Pattern p = Pattern.compile(json_regex);
        Matcher m = p.matcher(singleJsonObj);
        while(m.find())//While there are still pairs in the JSON object
        {
            //Get and process key value pair
            String pair = m.group(0);
            pair = pair.replaceAll("\"", "");//Remove inverted-commas
            System.err.println(pair);
            if(pair.contains(":"))
            {
                String[] kv_pair = pair.split(":");
                String var = kv_pair[0];
                String val = kv_pair[1];
                jsonableClassInstance.setVarValue(var,val);
            }
            //look for next pair
            singleJsonObj = singleJsonObj.substring(m.end());
            m = p.matcher(singleJsonObj);
        }
        //return jsonableClassInstance;
    }
}
