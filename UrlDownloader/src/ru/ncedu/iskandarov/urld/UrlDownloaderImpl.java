package ru.ncedu.iskandarov.urld;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class UrlDownloaderImpl implements UrlDownloader {

	private URL url = null;
	private String urlName = null;
	private String filePath = "";
	private String urlType = null;
	private String charset = null;
	private String domain = null;
	
	private final static int BUFFER_SIZE = 2048;
	
	
	
	@Override
	public void setUrl(String urlText) throws MalformedURLException, IOException {
		url = new URL(urlText);  //throws MalformedURLException
		charset = null;
		domain = null;
		findUrlType();
		setName();
	}
	
	
	
	@Override
	public void setUrl(String urlText, String urlType) throws MalformedURLException {
		
		if (urlText.indexOf("//") == 0) {
			urlText = "http:" + urlText;
		}
		url = new URL(urlText);  
		charset = null;
		domain = null;
		this.urlType = urlType;
		setName();
	}
	
	
	
	/**
	 * Finds url type (html, image, etc) and charset, if <code>urlType.equals("text/html")</code>. Then write it into private fields 
	 * <code>urlType</code> and <code>charset</code>
	 * @throws IOException - if can`t connect to url
	 */
	private void findUrlType() throws IOException {
		
		URLConnection urlc = null;
		urlc = url.openConnection();
		// next line needed to get a desktop version of web-site
		urlc.addRequestProperty("User-Agent", "Mozilla/6.0 (Windows NT 6.2; WOW64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1");
		urlc.setAllowUserInteraction( false );
		urlc.setDoInput( true );
		urlc.setDoOutput( false );
		urlc.setUseCaches( true );
		urlc.connect();
		
		String[] mime = urlc.getContentType().split("; ");
		
		urlType = mime[0];
		if (mime.length > 1) {
			charset = mime[1].substring(mime[1].indexOf('=') + 1);
	}	}
	
		
	
	/**
	 * Calculates <code>urlName</code> which satisfies all requirements of the task
	 */
	private void setName() {
		
		String text = url.toString();
		String[] tmp;
		urlName = null;
		
		if (!urlType.equals("text/html")) {
			tmp = text.split("[/]");
			urlName = tmp[tmp.length-1]; 	// example: http://www.abracadabra.com/folder/image.jpg
			if (urlName.contains("?")) {	// https://vk.com/css/al/common.css?29532151062 - can`t create file with '?' in name
				urlName = urlName.substring(0, urlName.indexOf('?'));
			}
			return;
		}
		
		if (text.contains("//")) {
			text = text.substring(text.indexOf("//") + 2);	// http://jj.com/* to jj.com/* - just help
		}
		
		if (text.contains("?")) {
			text = text.substring(0, text.indexOf('?'));
			tmp = text.split("/");
			if (tmp.length > 1) {
				urlName = tmp[tmp.length -1];	// name between ' ': http://ru.javasql.center/mod/quiz/ review.php ?attempt=2779
			}	
		} else {
			tmp = text.split("/");
			if (tmp.length > 1) {
				urlName = tmp[1];	// name between ' ': https://stackoverflow.com/ questions /6932369/inputstream-from-a-url
		}	}
		
		if (urlName == null)	{
			urlName = "index.html";
		} else {
			urlName = urlName + ".html";
	}	}
	
	
	
	@Override
	public String getUrl() throws IllegalStateException {
		if (url == null)	throw new IllegalStateException();
		return url.toString();
	}
	
	@Override
	public String getUrlType() {
		return urlType;
	}
	
	@Override
	public String getUrlName()  {
		return urlName;
	}
	
	@Override
	public void setPath(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public String getCharset() {
		return charset;
	}
	
	
	
	@Override
	public void open() throws IllegalStateException {
		if (url == null)	throw new IllegalStateException();
		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
		    desktop = Desktop.getDesktop();
		}
		
		try {
		    desktop.open(getTrueFile());
		} catch (IOException ioe) {
		    //ioe.printStackTrace();
			System.out.println("Opening issue. Perhaps, you do not have a suitable program");
	}	}

	
	
	@Override
	public void save() throws IllegalStateException, SecurityException, UnsupportedEncodingException, IOException, Exception{
		
		if (url == null)	throw new IllegalStateException();
		
		if (!urlType.equals("text/html")) {
			saveImport();
		} else {
			savePage();
	}	}
	
	
	
	/**
	 * Saves the import (img, css, js) with (IO)Streams.
	 * @throws SecurityException - if path refers to secret folder.
	 * @throws IOException - if an I/O error occurs while creating the input stream.
	 * @throws Exception - if choosing another file path needed.
	 */
	private void saveImport() throws SecurityException, IOException, Exception{
		File q = getTrueFile();
		if (q == null)	throw new Exception("So, you want to choose another file path. Restart program");
		if (!q.exists()) {
			try {
				q.createNewFile();
			} catch (SecurityException e) {
				throw new SecurityException("Choose another file path, SecurityException");
		}	}
		
		BufferedInputStream input = new BufferedInputStream (url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileOutputStream fos = new FileOutputStream (q.getAbsolutePath());
		
		byte[] buf = new byte[BUFFER_SIZE];
		
		int n = 0;
		while (-1 != (n = input.read(buf))) {
			out.write(buf, 0, n);
		}
		input.close();
		out.close();
		
		fos.write(out.toByteArray());
		fos.close();
		
	}
	
	
	
	
	/**
	 * Saves the html page to a hard disk with a right charset and filepath.
	 * @throws SecurityException - if path refers to secret folder.
	 * @throws IOException - if an I/O error occurs while creating the input stream.
	 * @throws UnsupportedEncodingException - if the named charset is not supported.
	 * @throws Exception - if choosing another filepath needed.
	 */
	private void savePage() throws UnsupportedEncodingException, SecurityException, IOException, Exception   {
		
		File q = getTrueFile();
		if (q == null)	throw new Exception("Choose another file path");
		if (!q.exists()) {
			try {
				q.createNewFile();
			} catch (SecurityException e) {
				throw new SecurityException("Choose another file path, SecurityException");
		}	}
		
		if (charset == null)
			charset = "utf-8";
		
		URLConnection urlc = url.openConnection(); // next line needed to get a desktop version of web-site
		urlc.addRequestProperty ("User-Agent", "Mozilla/6.0 (Windows NT 6.2; WOW64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1");
		InputStreamReader input = new InputStreamReader (urlc.getInputStream(), charset);
		OutputStreamWriter osw = new OutputStreamWriter (new FileOutputStream (q.getAbsolutePath()), charset);
		
		char[] buf = new char[BUFFER_SIZE];
		int n = 0;
		while (-1 != (n = input.read(buf, 0, BUFFER_SIZE))) {
			osw.write(buf, 0, n);
		}
		input.close();
		osw.close();
	
		
		String qPath = q.getAbsolutePath();
		File f = qPath.contains(".html") ?
				new File (qPath.substring (0, qPath.lastIndexOf (".html")) + "_files") :
					new File (qPath + "_files"); // it happens if filePath from cmd line args is like c:\folder\file (without .html)
		try {
			f.mkdir();
			f.createNewFile();
		} catch (SecurityException e) {
			throw new Exception("Choose another file path");
		}
		saveFiles(q,f);
	}
	
	
	
	/**
	 * @return filename which satisfies all requirements of the task
	 */
	private File getTrueFile() {
		
		if (filePath.equals("")) 
			return new File (getUrlName());
		
		File q = new File (filePath);
		if (!q.exists()) 
			return q;
		
		if (q.isDirectory()) 
			return new File (filePath + "\\" + getUrlName());
		
		System.out.println("File already exists! Choose '1' to save to this file or '2' to set another filePath, \n" + 
				"in this case you must run program with another command line arguments");
		int flag = 0;
		try {
			flag = System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		switch (flag - 48) {
		case 1:
			return q;
		case 2: 
			return null;
		default:
			System.out.println("FATAL ERROR!");
		}
		return null;
	}
	
	
	
	/**
	 * Saves media and import files to folder filemane_files <br>
	 * Uses Jsoup from ./lib/jsoup-1.10.3.jar
	 * @param directory is path to save
	 * @throws IOException - if the file could not be found, or read.
	 * @throws SecurityException - if security issues occurs.
	 */
	private void saveFiles(File page, File directory) throws IOException, SecurityException {
		
		findDomain();
		Document doc = Jsoup.parse(page, charset);
		Elements media = doc.select("img[src]");
        Elements imports = doc.select("link[href]");
        Elements scripts = doc.select("script[src]");
        
        Elements links = doc.select("a[href]");
		for (Element link : links) {
			if (!isHereDomain(link.attr("href"))) {
				link.attr("href", domain + link.attr("href"));
		}	}
		
        for (Iterator <Element> i = imports.iterator(); i.hasNext();) {
        	Element link = null;
       		link = i.next();
        	if (link.attr("rel").equals("alternate") || link.attr("rel").contains(".php")) {
        		i.remove();
        }   }
        
        saveAndChangeLinks(media, "src", directory);
        saveAndChangeLinks(imports, "href", directory);
        saveAndChangeLinks(scripts, "src", directory);
		
        OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter (new FileOutputStream (page.getAbsolutePath()), charset);
			osw.write(doc.toString());
		} catch (SecurityException e) {}
		finally {
			if (osw != null)
				osw.close();
	}	}
	
	
	
	/**
	 * Help function. Works with principle "download if can, and go to next else".
	 */
	private void saveAndChangeLinks(Elements list, String attribute, File directory) {
        
		UrlDownloaderImpl urld = new UrlDownloaderImpl();
		for (Element src : list) {
			//System.out.println("src *: " + src.attr(attribute));
           	try{
           		if(isHereDomain(src.attr(attribute))) {
           			urld.setUrl(src.attr(attribute), attribute);
           		} else {
           			urld.setUrl(domain + src.attr(attribute), attribute);
           		}
           	} catch (MalformedURLException e) {
           		continue;
           	}
           	
           	urld.setPath(directory.getAbsolutePath());
           	String attributeValue = "./" + directory.getName() + "/" + urld.getUrlName();
			src.attr(attribute, attributeValue);
			try {
				urld.saveImport();
			} catch (Exception e) {}
    }	}
	
	
	
	private void findDomain() {
		Pattern p = Pattern.compile(DOMAIN_PATTERN);
		Matcher m = p.matcher(url.toString());
		
		if (m.find()) {
			domain = m.group();
			domain = domain.substring(0, domain.length()-1);
		} else {
			domain = "";
	}	}
	
	
	
	private boolean isHereDomain(String someUrl) {
		Pattern p = Pattern.compile(DOMAIN_PATTERN);
		Matcher m = p.matcher(someUrl);
		
		if (m.find()) {
			return true;
		}
		if (someUrl.indexOf("//") == 0) {
			return true;
		}
		return false;
}	}


/*
Old functions:


	private void saveFiles(File page, File directory) throws IOException, Exception {
		
		findDomain();
		
		Document doc = Jsoup.parse(page, charset);
		Elements media = doc.select("img[src]");
        Elements imports = doc.select("link[href]");
        UrlDownloaderImpl urld = new UrlDownloaderImpl();
        for (Element src : media) {
           	if(isHereDomain(src.attr("src"))) {
           		urld.setUrl(src.attr("src"), "img");
           	} else {
           		urld.setUrl(domain + src.attr("src"), "img");
           	}
           	urld.setPath(directory.getAbsolutePath());
			try {
				urld.saveImport();
			} catch (IOException e) {}
        }
        
        int index = 0;
        for (Iterator <Element> i = imports.iterator(); i.hasNext(); index++) {
        	Element link = i.next();
        	if (link.attr("rel").equals("alternate")) {
        		imports.remove(index);
        		index--;
        	} else {
        		try {
        			if (isHereDomain(link.attr("href"))) {
        				urld.setUrl(link.attr("href"), "link");
        			} else {
        				urld.setUrl(domain + link.attr("href"), "link");
        			}
        		} catch (MalformedURLException e) {}
        		urld.setPath(directory.getAbsolutePath());
        		try {
        			urld.saveImport();
        		} catch (IOException e) {}
        		System.out.println(" * "+link.tagName() +" <" + link.attr("href") + "> (" + link.attr("rel") + ")");
        }	}
        
		changeLinks(doc, media, imports, page, directory);
	}

	private void changeLinks(Document doc, Elements media, Elements imports, File page, File directory) 
			throws MalformedURLException, IOException{
		UrlDownloaderImpl urld = new UrlDownloaderImpl();
		String attributeValue = null;
		
		for (Element src : media) {
			if(isHereDomain(src.attr("src"))) {
           		urld.setUrl(src.attr("src"), "img");
           	} else {
           		urld.setUrl(domain + src.attr("src"), "img");
           	}
			attributeValue = "./" + directory.getName() + "/" + urld.getUrlName();
			src.attr("src", attributeValue);
		}
		for (Element link : imports) {
			if(isHereDomain(link.attr("href"))) {
           		urld.setUrl(link.attr("href"), "link");
           	} else {
           		urld.setUrl(domain + link.attr("href"), "link");
           	}
			attributeValue = "./" + directory.getName() + "/" + urld.getUrlName();
			link.attr("href", attributeValue);
		}
		
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			if (!isHereDomain(link.attr("href"))) {
				link.attr("href", domain + link.attr("href"));
		}	}
		
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter (new FileOutputStream (page.getAbsolutePath()), charset);
			osw.write(doc.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (osw != null)
				osw.close();
	}	}
	
	
	private void setDomain(String domain) {
		this.domain = domain;
	}



	private void saveAndChangeScripts(Elements list, File directory) {
		UrlDownloaderImpl urld = new UrlDownloaderImpl();
		for (Element src : list) {
           	try{
           		if(isHereDomain(src.attr("src"))) {
           			urld.setUrl(src.attr("src"), "src");
           		} else {
           			urld.setUrl(domain + src.attr("src"), "src");
           		}
           	} catch (MalformedURLException e) {
           		continue;
           	}
           	
           	urld.setPath(directory.getAbsolutePath() + "\\" + urld.getUrlName() + ".без назв");
           	String attributeValue = "./" + directory.getName() + "/" + urld.getUrlName() + ".без назв";
			src.attr("src", attributeValue);
			try {
				urld.saveImport();
			} catch (Exception e) {}
	}	}
	


*/
