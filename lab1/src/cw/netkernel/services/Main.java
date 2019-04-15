package cw.netkernel.services;

import org.json.JSONObject;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import tools.LoaderAnalyzer;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.*;

public class Main extends StandardAccessorImpl {

    @Override
	public void onSource(INKFRequestContext context) throws Exception {

		String y = context.getThisRequest().getArgumentValue("year");
		String m = context.getThisRequest().getArgumentValue("month");
		String d = context.getThisRequest().getArgumentValue("day");
		String res = y + "/" + m + "/" + d;

		res = context.source("res:/cw-netkernel/nasa/" + res, String.class);
		ArrayList<int[]> tabs = LoaderAnalyzer.findProperNames(res);
		StringBuilder str = new StringBuilder(res);
		for(int[] el : tabs) {
			String with_spaces = res.substring(el[0], el[1]);
			String word = with_spaces.replace(' ', '_');
			String wikires = context.source("res:/cw-netkernel/wikimedia/" + word, String.class);
			str.replace(el[0], el[1], "<a href=\"" +wikires + "\">" + with_spaces + "</a>" );
		}
//		String p = context.getThisRequest().getArgumentValue("param");
		context.createResponseFrom(str.toString());
	}
}
