package com.spipi.spipimediaplayer.hubic;

import android.util.Pair;


import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenstackConnector {
	HubicAccess hubicAccess;
	String root;
	public OpenstackConnector(HubicAccess hubicAccess){
		this.hubicAccess= hubicAccess;
		this.root="";
		
	}
	public void setRoot(String root){
		
		this.root = root;
	}
	public Header[] DownloadFile(String distantPath, String modify){
		File file = new File(hubicAccess.getLocalStorage()+distantPath);
		System.out.println(file.getAbsolutePath());
	
		while(file.getAbsolutePath().startsWith(hubicAccess.getLocalStorage())){
				System.out.println("goood");
				if(file.isFile()){
					file.delete();
				
					/*try {
		    			Thread.sleep(2000);
		    		} catch (InterruptedException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}*/
				}
					
	        	file = file.getParentFile();
	        	
		}

		 return DownloadFile(distantPath,hubicAccess.getLocalStorage()+distantPath, modify) ;
		 
	}
	public List<NameValuePair> getAuthentificationHeaders(){
		List<NameValuePair> header = new ArrayList<NameValuePair>(1);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		return header;
	}
	public String getFullURL(){
		return hubicAccess.getOpenStackUrl()+"/"+root+"/";
	}
	public Header[] DownloadFile(String distantPath,String path, String modify){
		try {

			distantPath= URLEncoder.encode(distantPath, "UTF-8").replace("+", "%20");
	
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(1);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		
		Header[]ret =  conn.downloadFile(hubicAccess.getOpenStackUrl()+"/"+root+"/"+distantPath, header,path);
		//metadata
	
		
		
		
		return ret;
		 
	}
	
	
	public Pair<Header[], Integer> GetDetails(String distantPath, String modify){
		try {
			
			distantPath= URLEncoder.encode(distantPath, "UTF-8").replace("+", "%20");
			System.out.println(distantPath);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(2);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		header.add(new BasicNameValuePair("Range","bytes=2-2	"));

		 return conn.sendGetWithHeader(hubicAccess.getOpenStackUrl()+"/"+root+"/"+distantPath, header);
		 
	}
	public Pair<Header[], Integer> GetMetadata(String distantPath){
		try {
			
			distantPath= URLEncoder.encode(distantPath, "UTF-8").replace("+", "%20");
			System.out.println(distantPath);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(2);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));

		 return conn.sendHeadWithHeader(hubicAccess.getOpenStackUrl() + "/" + root + "/" + distantPath, header);
		 
	}
	public Header[] GetID3(String distantPath, String path){
		try {

			distantPath= URLEncoder.encode(distantPath, "UTF-8").replace("+", "%20");
	
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(2);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		header.add(new BasicNameValuePair("Range","bytes=0-1024	"));
		Header[] h = conn.downloadFile(hubicAccess.getOpenStackUrl()+"/"+root+"/"+distantPath, header,path);
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(path, "r" );
		
        byte[] headerbuf = new byte[10];
        file.read( headerbuf );
		 if ( headerbuf[0] != 'I' || headerbuf[1] != 'D' || headerbuf[2] != '3' )
	        {
	            file.close();
	            return null;
	        }
		 int tagsize = (headerbuf[9] & 0xFF) | ((headerbuf[8] & 0xFF) << 7 ) | ((headerbuf[7] & 0xFF) << 14 ) | ((headerbuf[6] & 0xFF) << 21 ) + 10;
			header = new ArrayList<NameValuePair>(2);
			header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
			header.add(new BasicNameValuePair("Range","bytes=0-"+tagsize));
			h = conn.downloadFile(hubicAccess.getOpenStackUrl()+"/"+root+"/"+distantPath, header,path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return h;
		 
	}
	public Header[] UploadFile(String distantPath,String localPath){
		try {
			distantPath=  new String(distantPath.getBytes(),"UTF-8");
			distantPath= URLEncoder.encode(distantPath, "UTF-8").replace("+", "%20");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(1);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		
		 return conn.putFile(hubicAccess.getOpenStackUrl()+"/"+root+"/"+distantPath,header,localPath);
		 
		 
	}
	public ArrayList<String> listFolderAndSubs(String path) throws Exception {
		if(!path.endsWith("/") && !path.isEmpty()) path = path+"/";
		try {
		
			path = URLEncoder.encode(path, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(1);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		String request;
		
		if(path.isEmpty())
			request = hubicAccess.getOpenStackUrl()+"/"+root+"";
		else
			request=hubicAccess.getOpenStackUrl()+"/"+root+"?prefix="+path+"";
		String[] res = conn.sendGetWithErrorCode(request, header);
		
		if (res==null || res[0].equals("401")){
			return null;
			
		}
		String response = res[1];
		
		ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(response.split("\\r?\\n")));
		System.out.println("size : "+arrayList.size());
		int nb= 1;
		String last = arrayList.get(arrayList.size()-1);
		while(arrayList.size()==10000*nb){//openstack send only 10000 names

			String newPath  ="";
			int i =0;
			for(String seg :last.split("/")) {
				newPath += (i!=0?"/":"") + URLEncoder.encode(seg, "UTF-8").replace("+", "%20");
				i++;
			}

			if(request.contains("?"))
				res = conn.sendGetWithErrorCode(request +"&marker="+newPath, header);
			else
				res = conn.sendGetWithErrorCode(request +"?marker="+newPath, header);
			if (res==null || res[0].equals("401")){	
				return null;
				
			}
			response = res[1];
			arrayList.addAll(Arrays.asList(response.split("\\r?\\n")));
			last = arrayList.get(arrayList.size()-1);

			nb++;
			
		}
		
	
		System.out.println("size : "+arrayList.size());
		return arrayList;
	}
	public ArrayList<String> listFolder(String path) throws Exception { //path must end with "/"
	
		if(!path.endsWith("/") && !path.isEmpty()) path = path+"/";
		try {
			path = URLEncoder.encode(path, "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection conn = new Connection();
		List<NameValuePair> header = new ArrayList<NameValuePair>(1);
		header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));
		String request;
		
		if(path.isEmpty())
			request = hubicAccess.getOpenStackUrl()+"/"+root+"?delimiter=/";
		else
			request=hubicAccess.getOpenStackUrl()+"/"+root+"?prefix="+path+"&delimiter=/";
		String[] res = conn.sendGetWithErrorCode(request, header);
		
		if (res[0].equals("401")){
			return null;
			
		}
		String response = res[1];
		ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(response.split("\\r?\\n")));
		ArrayList<String> arrayListTmp = new ArrayList<String>(arrayList);

		for (int i=0; i<arrayListTmp.size(); i++){
			System.out.println(arrayListTmp.get(i));
			if(arrayListTmp.get(i).endsWith("/")){
				for(int j=0; j<arrayList.size(); j++){
					
					if(arrayList.get(j).equals(arrayListTmp.get(i).substring(0, arrayListTmp.get(i).length()-1))){
						arrayList.remove(j);
					}
				}
				
			}
				
		}
		return arrayList;
		
		
	}
	public void listContainers() throws Exception {
			Connection conn = new Connection();
			List<NameValuePair> header = new ArrayList<NameValuePair>(1);
			header.add(new BasicNameValuePair("X-Auth-Token", hubicAccess.getOpenStackAccessToken()));

			System.out.println(conn.sendGet(hubicAccess.getOpenStackUrl(), header));
		
	  

	}
}
