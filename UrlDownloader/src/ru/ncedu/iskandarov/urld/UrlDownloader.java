package ru.ncedu.iskandarov.urld;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

/**
 * Version 1.0 <br>
 * 
 * The program satisfies all requirements of the task UrlDownloader.<br>
 * The program does not work perfectly on "hard" and "high-tech" web-sites which uses tricky scripts.<br>
 * 
 * @author Azamat Iskandarov
 */

public interface UrlDownloader {

	/**
	 * It needs to find domain in url text.
	 */
	public static final String DOMAIN_PATTERN = "(https?://)(www\\.)?([^/]+/?)";
	
	/**
	 * Creates URL object with urlText
	 * @param urlText is not null.
	 * @throws MalformedURLException - if no protocol is specified, or an unknown protocol is found, or spec is null.
	 * @throws IOException - if an I/O issues occurs.
	 */
	public void setUrl (String urlText) throws MalformedURLException, IOException;
	
	/**
	 * Creates URL object with url = urlText and type = urlTtype.
	 * @param urlText is not null.
	 * @param urlType is not null.
	 * @throws MalformedURLException - if no protocol is specified, or an unknown protocol is found, or spec is null.
	 * @throws IOException - if an I/O issues occurs.
	 */
	public void setUrl(String urlText, String urlType) throws MalformedURLException, IOException;
	
	/**
	 * @return urlText
	 * @throws IllegalStateException if url was not set.
	 */
	public String getUrl() throws IllegalStateException;
	
	/**
	 * @return urlName or NULL if url was not set.
	 */
	public String getUrlName();
	
	/**
	 * @return urlType or NULL if url was not set.
	 */
	public String getUrlType();
	
	/**
	 * It`s just a setter. It does not check <code>filePath> to valid.
	 * @param filePath must be valid
	 */
	public void setPath (String filePath);
	
	
	/**
	 * Open saved url from hard disk
	 */
	public void open();
	
	/**
	 * Saves the url to hard disk. If filePath was not set then directory to save is workspace
	 * @throws IllegalStateException if url was not set.
	 * @throws SecurityException - if security issues occurs.
	 * @throws UnsupportedEncodingException - if the named charset is not supported.
	 * @throws IOException any problems with I/O.
	 * @throws Exception if program`s stop needed for change filePath.
	 */
	public void save() throws SecurityException, IllegalStateException, UnsupportedEncodingException, IOException, Exception;
	
	
	public String getCharset();
}
