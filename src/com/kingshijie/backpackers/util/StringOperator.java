package com.kingshijie.backpackers.util;

import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;

public class StringOperator {
	public static String mergeParams(String uri,List<NameValuePair> params){
		StringBuilder builder = new StringBuilder();
		Iterator<NameValuePair> it = params.iterator();
		if(it.hasNext()){
			NameValuePair param = it.next();
			builder.append(param.getName() + "=" + param.getValue());
		}
		while(it.hasNext()){
			NameValuePair param = it.next();
			builder.append("&" + param.getName() + "=" + param.getValue());
		}
		return uri + "?" + builder.toString();
	}
}
