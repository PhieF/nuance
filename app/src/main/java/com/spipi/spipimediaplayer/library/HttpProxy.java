package com.spipi.spipimediaplayer.library;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * This is simple HTTP local server for streaming InputStream to apps which are capable to read data from url.
 * Random access input stream is optionally supported, depending if file can be opened in this mode. 
 */
public class HttpProxy {
	private static final String TAG = "HttpProxy";
	private final Uri mUri;
	private String fileMimeType;
	private long length;
	private static final int BUFFER_SIZE = 8192;
	private ServerSocket serverSocket;
	private Thread mainThread;
	private FileInfo mMetaFile;

	/**
	 * Some HTTP response status codes
	 */
	private static final String 
	HTTP_BADREQUEST = "400 Bad Request",
	HTTP_416 = "416 Range not satisfiable",
	HTTP_INTERNALERROR = "500 Internal Server Error";
	private ArrayList<FileInfo> mSubList;



	public HttpProxy(FileInfo f, String forceMimeType) throws IOException{
		mMetaFile = f;
		mUri= f.getUri();
		fileMimeType = forceMimeType!=null ? forceMimeType : "*/*";
        serverSocket = new ServerSocket(0);
		mainThread = new Thread(new Runnable(){
			public void run(){
				try{
					while(true) {
						Socket accept = serverSocket.accept();
						new HttpSession(accept,fileMimeType);
					}
				}catch(IOException e){	}
			}

		});
		mainThread.setName("Stream over HTTP");
		mainThread.setDaemon(true);
		mainThread.start();
	}
    public HttpProxy(final Uri uri, final String forceMimeType) throws IOException{
		mUri = uri;
        fileMimeType = forceMimeType!=null ? forceMimeType : "*/*";
        serverSocket = new ServerSocket(0);
        mainThread = new Thread(new Runnable(){
            public void run(){

                try{
                    while(true) {
                        Socket accept = serverSocket.accept();
                        new HttpSession(accept,fileMimeType);
                    }
                }catch(IOException e){
                }
            }

        });
        mainThread.setName("Stream over HTTP");
        mainThread.setDaemon(true);
        mainThread.start();


    }


	private class HttpSession implements Runnable{
		private boolean canSeek;
		private InputStream is;
		private final Socket socket;
		private Properties mPre=null;
		private int mRlen=-1;
		private InputStream mInS=null;
		private String fileMimeType =""; // this might be changed we a subtitle is sent

		HttpSession(Socket s, String fileMimeType){
			this.fileMimeType = fileMimeType;
			socket = s;
			Log.i(TAG,"Stream over localhost: serving request on "+s.getInetAddress());
			Thread t = new Thread(this, "Http response");
			t.setDaemon(true);
			t.start();
		}

		public void run(){

			try{
				openInputStream();
				handleResponse(socket);
			}catch(IOException e){
						}finally {
				try {
					socket.close();
				} catch(Exception e) {}
				if(is!=null) {
					try{
						is.close();
					}catch(IOException e){

					}
				}
			}
		}

		private void openInputStream() throws IOException{


			boolean needsToStream = false;
			long startFrom = 0;
			mPre = new Properties();
			mInS = socket.getInputStream();
			String path=null;
			if(mInS != null){
				byte[] buf = new byte[BUFFER_SIZE];
				mRlen = mInS.read(buf, 0, buf.length);
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, mRlen);
				BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
				try {
					String encodedPath;
					if((encodedPath=decodeHeader(socket, hin, mPre))!=null){
						String range = mPre.getProperty("range");
						if(range!=null){
							range = range.substring(6);

							int minus = range.indexOf('-');
							String startR = range.substring(0, minus);
							startFrom = Long.parseLong(startR);
							needsToStream = true;
						}
						path = Uri.decode(encodedPath);
					}
				} catch (InterruptedException e) { }
			}

			try {
				canSeek = true;

				/*
				 * first, we retrieve main metafile
				 */
				if(mMetaFile==null&&mUri!=null){
					try {
						mMetaFile = FileInfoFactory.getFileInfoForUrl(mUri);
					}
					catch(Exception e){

					}

				}


				if(mMetaFile==null)
					return;
				/*
					some players like MXPlayer try to find subs associated with http urls

				 */

				FileInfo fileInfo =null;

				if(fileInfo ==null)
					fileInfo = mMetaFile;
				length = fileInfo.length();

				is =  fileInfo.getFileEditorInstance(null).getInputStream(startFrom);
			}  catch (Exception e) {
				e.printStackTrace();

			}

		}

		private void handleResponse(Socket socket) {
			try{
				InputStream inS = socket.getInputStream();
				if(inS == null&&mInS==null)
					return;
				else if(inS == null)
					inS = mInS; //we have already touched the stream

				byte[] buf = new byte[BUFFER_SIZE];
				int rlen =mRlen;
				if(mRlen<0)
					rlen= inS.read(buf, 0, buf.length);
				if(rlen < 0&&mRlen<0)
					return;
				else if (rlen < 0)
					rlen = mRlen; // we already have the length (ssh)

				// Create a BufferedReader for parsing the header.
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
				BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
				Properties pre = new Properties();

				// Decode the header into params and header java properties
				boolean decode = decodeHeader(socket, hin, pre)!=null;
				if(!decode&&mPre==null)
					return;
				else if(!decode)
					pre = mPre; // properties have already been decoded (ssh)

				String range = pre.getProperty("range");

				Properties headers = new Properties();
				if(length!=-1)
					headers.put("Content-Length", String.valueOf(length));
				headers.put("Accept-Ranges", canSeek ? "bytes" : "none");
				long sendCount;
				String status;
				if(range==null || !canSeek) {
					status = "200 OK";
					sendCount = length;
				}else {
					if(!range.startsWith("bytes=")){
						sendError(socket, HTTP_416, null);
						return;
					}

					range = range.substring(6);
					long startFrom = 0, endAt = -1;
					int minus = range.indexOf('-');
					if(minus > 0){
						try{
							String startR = range.substring(0, minus);
							startFrom = Long.parseLong(startR);
							String endR = range.substring(minus + 1);
							endAt = Long.parseLong(endR);
						}catch(NumberFormatException nfe){}
					}

					if(startFrom >= length){
						sendError(socket, HTTP_416, null);
						inS.close();
						return;
					}
					if(endAt < 0)
						endAt = length - 1;
					sendCount = (endAt - startFrom + 1);
					if(sendCount < 0)
						sendCount = 0;
					status = "206 Partial Content";

					/* else
            	   is.skip(startFrom);*/
            	   headers.put("Content-Length", "" + sendCount);
            	   String rangeSpec = "bytes " + startFrom + "-" + endAt + "/" + length;
            	   headers.put("Content-Range", rangeSpec);
				}

				sendResponse(socket, status, fileMimeType, headers, is, sendCount, buf, null);

			}catch(IOException ioe){

				try{
					sendError(socket, HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				}catch(Throwable t){
				}
			}catch(InterruptedException ie){


			}
		}

		/**
		 * decode header and returns requested path
		 * @param socket
		 * @param in
		 * @param pre
		 * @return
		 * @throws InterruptedException
		 */
		private String decodeHeader(Socket socket, BufferedReader in, Properties pre) throws InterruptedException{
			String path=null;
			try{
				// Read the request line
				String inLine = in.readLine();
				if(inLine == null)
					return null;
				StringTokenizer st = new StringTokenizer(inLine);
				if(!st.hasMoreTokens())
					sendError(socket, HTTP_BADREQUEST, "Syntax error");

				String method = st.nextToken();
				if(!method.equals("GET"))
					return null;

				if(!st.hasMoreTokens())
					sendError(socket, HTTP_BADREQUEST, "Missing URI");
				path = st.nextToken();
				while(true) {
					String line = in.readLine();
					if(line==null)
						break;

					int p = line.indexOf(':');
					if(p<0)
						continue;
					final String atr = line.substring(0, p).trim().toLowerCase();
					final String val = line.substring(p + 1).trim();
					pre.put(atr, val);
				}
			}catch(IOException ioe){
				sendError(socket, HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
			return path;
		}
	}


	/**
	 * @param fileName is display name appended to Uri, not really used (may be null), but client may display it as file name.
	 * @return Uri where this stream listens and servers.
	 */
	public Uri getUri(String fileName){
		int port = serverSocket.getLocalPort();
		String url = "http://localhost:"+port;
		if(fileName!=null)
			url += '/'+fileName;
		return Uri.parse(url);
	}

	public void close(){
		Log.d(TAG,"Closing stream over http");
		try{
			serverSocket.close();
		}catch(Exception e){

		}
	}

	/**
	 * Returns an error message as a HTTP response and
	 * throws InterruptedException to stop further request processing.
	 */
	private void sendError(Socket socket, String status, String msg){
		try {
			sendResponse(socket, status, "text/plain", null, null, 0, null, msg);
		} catch (IOException e) {}
	}

	private void copyStream(InputStream in, OutputStream out, byte[] tmpBuf, long maxSize) throws Exception{
		Log.d(TAG, "copyStream");
		int count;

		while(maxSize>0){
			count = (int) Math.min(maxSize, (long)tmpBuf.length);
			count = in.read(tmpBuf, 0, count);
			if(count<0)
				break;
			out.write(tmpBuf, 0, count);
			out.flush();
			maxSize -= count;
		}
	}
	/**
	 * Sends given response to the socket, and closes the socket.
	 */
	private void sendResponse(Socket socket, String status, String mimeType, Properties header, InputStream isInput, long sendCount, byte[] buf, String errMsg) throws IOException {
		Log.d(TAG, "sendResponse");
		BufferedInputStream bin = null;
		try{
			OutputStream out = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(out);
			bin = new BufferedInputStream(isInput, BUFFER_SIZE*10);
			{
				String retLine = "HTTP/1.0 " + status + " \r\n";
				pw.print(retLine);
			}
			if(mimeType!=null) {
				String mT = "Content-Type: " + mimeType + "\r\n";
				pw.print(mT);
			}
			if(header != null){
				Enumeration<?> e = header.keys();
				while(e.hasMoreElements()){
					String key = (String)e.nextElement();
					String value = header.getProperty(key);
					String l = key + ": " + value + "\r\n";
					pw.print(l);
				}
			}
			pw.print("\r\n");
			pw.flush();
			if(isInput != null){
				copyStream(bin, out, buf, sendCount);
			}
			else if(errMsg!=null) {
				pw.print(errMsg);
				pw.flush();
			}
			out.flush();
			out.close();
		} catch(Exception e){

		}finally {
			try{
				socket.close();
			}catch(Throwable t){}
			if (bin != null)
				try{
					bin.close();
					
				}catch(Throwable t){}
		}
	}
}



