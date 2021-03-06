/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.github.cxt.Myjersey.jerseycore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Path("/file")
public class FileResource {
	
	private static String CHARSET = "UTF-8";
	private static int MAX_NAME_SIZE = 50;
	
	@Path("put/{path}")
	@PUT
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String putFile(InputStream inputStream, @PathParam("path") String name, @Context HttpServletRequest request) throws IOException {
		String path = request.getServletContext().getRealPath("/");
		path += File.separator + "data" + File.separator + name;
		File file = new File(path);
		try {
			FileUtils.copyInputStreamToFile(inputStream, file);
		} catch (IOException ex) {
			ex.printStackTrace();
			return "{\"success\": false}";
		}
		return "{\"success\": true}";
	}
	
	@Path("store/{path:.*}")
	@GET
	public Response storeInfo(@PathParam("path") String path, @Context HttpServletRequest request) throws Exception{
		File file = new File(request.getServletContext().getRealPath("data/" + path));
		if(file.exists()){
			if(file.isDirectory()){
				return listfile(path, file);
			}
			else{
				String mt = new MimetypesFileTypeMap().getContentType(file);
				//文件名兼容性可能不够,兼容性好点参考download2
		        return Response.ok(file, mt)
		                .header("Content-disposition","attachment;filename=" + file.getName() + ";filename*=UTF-8''" + URLEncoder.encode(file.getName(), "UTF-8")) 
		                .header("ragma", "No-cache").header("Cache-Control", "no-cache").build();
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<html>").append("\r\n")
		.append("<head><title>404 Not Found</title></head>").append("\r\n")
		.append("<body bgcolor=\"white\">").append("\r\n")
		.append("<center><h1>404 Not Found</h1></center>").append("\r\n")
		.append("</body>").append("\r\n")
		.append("</html>");
		return Response.ok(sb.toString()).header("Content-Type", "text/html;charset=utf-8").build();
	}
	
	private Response listfile(String path, File dir) throws UnsupportedEncodingException{
		StringBuilder sb = new StringBuilder();
		String title = replace(path);
		sb.append("<html>").append("\r\n")
		.append("<head><title>Index of " + title + "</title></head>").append("\r\n")
		.append("<body bgcolor=\"white\">").append("\r\n")
		.append("<h1>Index of " + title +"</h1><hr><pre><a href=\"../\">../</a>").append("\r\n");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
		for(File f : dir.listFiles()){
			byte[] bytes = f.getName().getBytes(CHARSET);
			String name = null;
			StringBuilder fill = new StringBuilder();
			if(bytes.length > MAX_NAME_SIZE){
				name = new String(bytes, 0, MAX_NAME_SIZE - 3) + "..>";
			}
			else {
				name = f.getName();
				for(int i = bytes.length; i < MAX_NAME_SIZE; i++){
					fill.append(" ");
				}
			}
			sb.append("<a href=\"").append(URLEncoder.encode(f.getName(), CHARSET));
			if(f.isDirectory()){
				sb.append("/");	
			}
			sb.append("\">")
			.append(replace(name))
			.append("</a>")
			.append(fill)
			.append(sdf.format(new Date(f.lastModified())))
			.append("               ");
			if(f.isFile()){
				sb.append(f.length());
			}
			sb.append("\r\n");
		}
		sb.append("</pre><hr></body>").append("\r\n")
		.append("</html>");
		return Response.ok(sb.toString()).header("Content-Type", "text/html;charset=utf-8").build();
	}
	
	private static String replace(String str){
		str = str.replace("&", "&amp;");
		str = str.replace("<", "&lt;");
		str = str.replace(">", "&gt;");
		str = str.replace("\"", "&quot;");
		return str;
	}
	
    
//	//使用存储临时文件(从流中直接读取文件并保存到临时文件,新的流是从临时文件中读取的)
//	@Path("upload")
//	@POST
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	public String uploadFile(@FormDataParam("file") InputStream inputStream,
//			@FormDataParam("file") FormDataContentDisposition disposition, 
//			@FormDataParam("p1") String p1,
//			@Context HttpServletRequest request) throws IOException {
//		//浏览器默认不会带,httpclent可以带
//		System.out.println(request.getCharacterEncoding());
//		System.out.println(p1);
//		
//		String fileName = new String(disposition.getFileName().getBytes("ISO8859-1"), CHARSET);
//		if(fileName != null && !fileName.trim().equals("")){
//			String name = Calendar.getInstance().getTimeInMillis() + fileName;
//			String path = request.getServletContext().getRealPath("/");
//			path += File.separator + "data" + File.separator + name;
//			File file = new File(path);
//			try {
//				FileUtils.copyInputStreamToFile(inputStream, file);
//			} catch (IOException ex) {
//				ex.printStackTrace();
//				return "{\"success\": false}";
//			}
//		}
//		return "{\"success\": true}";
//	}
	
	
	//使用存储临时文件(从流中直接读取文件并保存到临时文件,新的流是从临时文件中读取的)
	@Path("upload")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public String uploadFile(FormDataMultiPart form, @Context HttpServletRequest request) throws IOException {
		//浏览器默认不会带,httpclent可以带
		System.out.println(request.getCharacterEncoding());
		Map<String, List<FormDataBodyPart>> map = form.getFields();
		for(String key : map.keySet()){
			FormDataBodyPart filePart = form.getField(key);
			//流会自己关闭掉
			InputStream inputStream = filePart.getValueAs(InputStream.class);  
			FormDataContentDisposition disposition = filePart.getFormDataContentDisposition();
			System.out.println(filePart.getMediaType());
			if(disposition.getFileName() == null){//文本内容
				System.out.println(key + "!" + filePart.getValue());
			}
			else {
				String fileName = new String(disposition.getFileName().getBytes("ISO8859-1"), CHARSET);
			    if(fileName != null && !fileName.trim().equals("")){
					String name = Calendar.getInstance().getTimeInMillis() + fileName;
					String path = request.getServletContext().getRealPath("/");
					path += File.separator + "data" + File.separator + name;
					File file = new File(path);
					try {
						FileUtils.copyInputStreamToFile(inputStream, file);
					} catch (IOException ex) {
						ex.printStackTrace();
						return "{\"success\": false}";
					}
				}
			}
		}		
		return "{\"success\": true}";
	}
	
	
	//从流中直接读取文件
	@Path("upload2")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public String uploadFile(@Context HttpServletRequest request) throws IOException {
		//浏览器默认不会带,httpclent可以带
		System.out.println(request.getCharacterEncoding());
		ServletFileUpload upload = new ServletFileUpload();
		upload.setHeaderEncoding(CHARSET);
		try{
			FileItemIterator fileIterator = upload.getItemIterator(request);
			while (fileIterator.hasNext()) {
				FileItemStream item = fileIterator.next();
				InputStream is = item.openStream();
				try{
					if (!item.isFormField()){
						String fileName = item.getName();
						if(fileName == null || fileName.trim().equals("")){
							continue;
						}				
						String name = Calendar.getInstance().getTimeInMillis() + fileName;
						String path = request.getServletContext().getRealPath("/");
						path += File.separator + "data" + File.separator + name;
						File file = new File(path);
						FileUtils.copyInputStreamToFile(is, file);
					}
					else {
						System.out.println(Streams.asString(is, CHARSET));
					}
				}finally{
					if(null != is){
						try {
							is.close();
						} catch (IOException ignore) {
						}
					}
				}
			}
			return "{\"success\": true}";
		}catch(IOException | FileUploadException e){
			return "{\"success\": false}";
		} 
	}
	
	
	@Path("download")
	@GET
	public Response downloadFile(@Context HttpServletRequest request) throws IOException {
		File file = new File(request.getServletContext().getRealPath("index.html"));
		String mt = new MimetypesFileTypeMap().getContentType(file);
        return Response
                .ok(file, mt)
                .header("Content-disposition","attachment;filename=" + file.getName()) 
                .header("ragma", "No-cache").header("Cache-Control", "no-cache").build();
		
	}
	
	@Path("download2")
	@GET
	public Response downloadFile(@Context HttpServletResponse response, @Context HttpServletRequest request) throws IOException {
		final File file = new File(request.getServletContext().getRealPath("index.html"));
		final InputStream responseStream = new FileInputStream(file);
		StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
            	try{
	                int length;
	                byte[] buffer = new byte[1024 * 10];
	                while((length = responseStream.read(buffer)) != -1) {
	                    out.write(buffer, 0, length);
	                    out.flush();
	                }
            	}finally{
            		responseStream.close();
            		//file.delete();
            	}
            }   
        };
        
        
        String filename = file.getName();
        String userAgent = request.getHeader("User-Agent");
        //IE11 User-Agent字符串:Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko
        //IE6~IE10版本的User-Agent字符串:Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.0; Trident/6.0)
         
        if (userAgent != null && (userAgent.toLowerCase().indexOf("msie") > 0 || userAgent.indexOf("like Gecko") > 0)){
        	filename = URLEncoder.encode(filename, "UTF-8");//IE浏览器
        }else {
        //先去掉文件名称中的空格,然后转换编码格式为utf-8,保证不出现乱码,
        //这个文件名称用于浏览器的下载框中自动显示的文件名
        	filename = new String(filename.replaceAll(" ", "").getBytes(), "ISO8859-1");
        //firefox浏览器
        //firefox浏览器User-Agent字符串: 
        //Mozilla/5.0 (Windows NT 6.1; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0
        }
        //http://blog.csdn.net/candyguy242/article/details/17449191
        return Response.ok(output).header("Content-Disposition", "attachment; filename=" + filename + ";filename*=UTF-8''" 
        		+ URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20")).encoding("UTF-8").build();
	}
	
	
	@Path("download3")
	@GET
	public Response downloadFile() throws IOException {
		String file = "test.txt";
		Response.ResponseBuilder responseBuilder = Response.status(200);
		//如转向连接里有head头里有文件信息,则会冲突,只能存一。
		responseBuilder.header("Content-Disposition", "attachment; filename=\"" + file + "\"");
		responseBuilder.header("Content-Type", "application/octet-stream");
		//https://www.nginx.com/resources/wiki/start/topics/examples/xsendfile/
		responseBuilder.header("X-Accel-Redirect", "/data/" + file);
		return responseBuilder.build();
		
	}
	
	
    @Path("show")
    @GET
    public Response show(@Context HttpServletRequest request) throws FileNotFoundException {
    	final File file = new File(request.getServletContext().getRealPath("index.html"));
		final InputStream responseStream = new FileInputStream(file);
		StreamingOutput output = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
            	try{
	                int length;
	                byte[] buffer = new byte[1024 * 10];
	                while((length = responseStream.read(buffer)) != -1) {
	                    out.write(buffer, 0, length);
	                    out.flush();
	                }
            	}finally{
            		responseStream.close();
            	}
            }   
        };
        return Response.ok(output).header("Content-Type", "text/plain").build();
    }
}
