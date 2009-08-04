package org.odk.manage.android.comm;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.odk.manage.android.Constants;
import org.odk.manage.android.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An adapter for file handling.
 * 
 * @author alerer@google.com (Adam Lerer)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 *
 */
public class FileHandler {
  
  private Context ctx;
  
  public FileHandler(Context ctx){
    this.ctx = ctx;
  }
  
  /**
   * Creates and opens a directory on the SD card.
   * @param path A path to the directory
   * @return A file pointer to the directory.
   * @throws IOException If there is an SD card error or the directory could not be created.
   */
  public File getDirectory(String path) throws IOException{

    // check to see if there's an sd card.
    String cardstatus = Environment.getExternalStorageState();
    if (cardstatus.equals(Environment.MEDIA_REMOVED)
            || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
            || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
            || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
        throw new IOException(ctx.getString(R.string.sdcard_error));
    }

    // if storage directory does not exist, create it.
    boolean made = true;
    File mRoot = new File(path);
    if (!mRoot.exists()) {
        made = mRoot.mkdirs();
    }

    if (!made)
        throw new IOException(ctx.getString(R.string.directory_error, path));
    return mRoot;
  }

  /**
   * 
   * @param f A file or directory.
   * @return A list containing the file and its children that have valid filenames.
   */
  public List<File> getFiles(File f) {
    List<File> files = new ArrayList<File>();
    if (f.isDirectory()) {
        File[] childs = f.listFiles();
        for (File child : childs) {
            files.addAll(getFiles(child));
        }
        return files;
    } else {
      String filename = f.getName();
      if (filename.matches(Constants.VALID_FILENAME)) {
          files.add(f);
      }
    }
    return files;
  }
  
  /**
   * Downloads a valid form from a url to a directory. If the file exists, it is 
   * overwritten.
   * @return Pointer to the file downloaded, or null if unsuccessful
   */
  public File getFormFromConnection(URLConnection c, File downloadDirectory) throws IOException{
    File f =  getFileFromConnection(c, downloadDirectory);
    if (f.getName().matches(Constants.VALID_FILENAME)) {
      return f;
    }
    else {
      Log.i(Constants.TAG,"Form name was not valid for download: " + f.getName());
      f.delete();
      return null;
    }
  }
  
  /**
   * Downloads a file from a url to a directory
   * @return Filename of the file downloaded, or null if unsuccessful
   */
  public File getFileFromConnection(URLConnection c, File downloadDirectory) throws IOException{
    // prevent deadlock when connection is invalid
    
    InputStream is = c.getInputStream();
    String filename = getFilename(c);
    filename = filename.substring(filename.lastIndexOf('/') + 1);
    File f = new File(downloadDirectory + "/" + filename);
    
    OutputStream os = new FileOutputStream(f);
    byte buf[] = new byte[1024];
    int len;
    while ((len = is.read(buf)) > 0)
        os.write(buf, 0, len);
    os.flush();
    os.close();
    is.close();
    return f;
  }

  /**
   * Gets the filename for a file downloaded from a URLConnection. First checks the 
   * Content-disposition header for a filename; if one is not found, guesses based 
   * on the URL.
   * @param c
   */
  private String getFilename(URLConnection c){
    String disposition = c.getHeaderField("Content-Disposition");
    String dispFilename = getFilenameFromDisposition(disposition);
    if (dispFilename != null) {
        return dispFilename;
    }
    try {
      String url = c.getURL().toString();
      return url.substring(url.lastIndexOf('/') + 1);
    } catch (IndexOutOfBoundsException e) {
      return "";
    }
  }
  
  /**
   * 
   * @param disp The content-disposition field of the HTTP response.
   * @return The attachment filename, or null if this Content-Disposition field did not contain an attachment
   */
  private String getFilenameFromDisposition(String disp){
    if (disp == null) {
      return null;
    }
    //we could make the patterns class/object vars to make it faster
    Pattern p1 = Pattern.compile("filename=(.)");
    Matcher m1 = p1.matcher(disp);
    
    if (!m1.find() || m1.groupCount() < 1) {
      return null;
    }
    String delim = m1.group(1);
    
    Pattern p2 = null;
    if ("'".equals(delim) || "\"".equals(delim)) {
      p2 = Pattern.compile("filename=" + delim + "([^" + delim + "]*)" + delim);
    } else {
      p2 = Pattern.compile("filename=([^ ]*)( .*)?$");
    }
    Matcher m2 = p2.matcher(disp);
    if (!m2.find() || m2.groupCount() < 1) {
      return null;
    }
    return m2.group(1);
  }

}
