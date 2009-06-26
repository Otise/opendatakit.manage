package org.google.android.odk.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.google.android.odk.manage.Constants;
import org.google.android.odk.manage.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
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
      if (filename.matches(SharedConstants.VALID_FILENAME)) {
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
  public File getFormFromUrl(URL u, File downloadDirectory) throws IOException{
    String filename = u.getFile();
    filename = filename.substring(filename.lastIndexOf('/') + 1);
    if (filename.matches(SharedConstants.VALID_FILENAME)) {
      Log.i(Constants.TAG,"Downloading form: " + filename);
      return getFileFromUrl(u, downloadDirectory);
    }
    else {
      Log.i(Constants.TAG,"Form name was not valid for download: " + filename);
      return null;
    }
  }
  
  /**
   * Downloads a file from a url to a directory
   * @return Filename of the file downloaded, or null if unsuccessful
   */
  public File getFileFromUrl(URL u, File downloadDirectory) throws IOException{
    // prevent deadlock when connection is invalid
    URLConnection c = u.openConnection();
    c.setConnectTimeout(SharedConstants.CONNECTION_TIMEOUT);
    c.setReadTimeout(SharedConstants.CONNECTION_TIMEOUT);
    InputStream is = c.getInputStream();
    
    String filename = u.getFile();
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

}
