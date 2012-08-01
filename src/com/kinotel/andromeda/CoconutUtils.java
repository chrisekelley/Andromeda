package com.kinotel.andromeda;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


public class CoconutUtils extends Activity {
	
	/**
	 * Reference to the Android context
	 */
	private static Context ctx;
	
	protected static final String TAG = "CoconutActivity";

	

	
	public static String readAsset(AssetManager assets, String path) throws IOException {
		InputStream is = assets.open(path);
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();
		return new String(buffer);
	}

    public static String readFile(File file) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    public static void writeFile(File file, String data) throws IOException {
    	FileWriter fstream = new FileWriter(file);
    	BufferedWriter out = new BufferedWriter(fstream);
    	out.write(data);
    	out.close();
    }
    
    public static String md5(String input){
        String res = "";
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            String tmp = "";
            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    res += "0" + tmp;
                } else {
                    res += tmp;
                }
            }
        } catch (NoSuchAlgorithmException ex) {}
        return res;
    }
    

    /**
     * extracts a zip archive from the assets dir. Copies to a writable dir first.
     * @param ctx TODO
     * @param destinationDirectory TODO
     * @param argv
     * @throws Exception 
     */
    public static void unZipFromAssets (Context ctx, String file, String destinationDirectory) throws Exception {
    	String destinationFilename = extractFromAssets(ctx, file, destinationDirectory);		
    	try {
    		unZip(destinationFilename, destinationDirectory);
    	} catch (Exception e) {
    		throw new Exception(e);
    	}
    }


	public static String extractFromAssets(Context ctx, String file, String destinationDirectory) throws IOException, FileNotFoundException {
		final int BUFFER = 2048;
    	BufferedOutputStream dest = null;
    	AssetManager assetManager = ctx.getAssets();
    	InputStream in = assetManager.open(file);	
    	String destinationFilename = destinationDirectory + File.separator + file;
		OutputStream out = new FileOutputStream(destinationFilename);
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
		return destinationFilename;
	}
    
    /**
     * extracts a zip archive. Does not handle directories
     * kudos: http://java.sun.com/developer/technicalArticles/Programming/compression/
     * @param argv
     * @throws IOException 
     */
    public static void unZipNoDirs (String file, String destinationDirectory) throws IOException {
    	final int BUFFER = 2048;
    	BufferedOutputStream dest = null;
    	FileInputStream fis = new FileInputStream(file);
    	ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    	ZipEntry entry;
    	while((entry = zis.getNextEntry()) != null) {
    		int count;
    		byte data[] = new byte[BUFFER];
    		// write the files to the disk
    		String destinationFilename = destinationDirectory + File.separator + entry.getName();
    		FileOutputStream fos = new FileOutputStream(destinationFilename);
    		dest = new BufferedOutputStream(fos, BUFFER);
    		while ((count = zis.read(data, 0, BUFFER)) != -1) {
    			dest.write(data, 0, count);
    		}
    		dest.flush();
    		dest.close();
    	}
    	zis.close();
    }
    
    /**
     * kudos: http://www.jondev.net/articles/Unzipping_Files_with_Android_(Programmatically)
     * @param zipFile
     * @param destinationDirectory
     */
    public static void unZip(String zipFile, String destinationDirectory) { 
    	try  { 
    		FileInputStream fin = new FileInputStream(zipFile); 
    		ZipInputStream zin = new ZipInputStream(fin); 
    		ZipEntry ze = null; 
    		while ((ze = zin.getNextEntry()) != null) { 
    			Log.v("Decompress", "Unzipping " + ze.getName()); 
    			String destinationPath = destinationDirectory + File.separator + ze.getName();
    			if(ze.isDirectory()) { 
    				dirChecker(destinationPath); 
    			} else { 
    				FileOutputStream fout;
					try {
						File outputFile = new File(destinationPath);
						if (!outputFile.getParentFile().exists()){
							dirChecker(outputFile.getParentFile().getPath());
						}
						fout = new FileOutputStream(destinationPath);
	    				for (int c = zin.read(); c != -1; c = zin.read()) { 
	    					fout.write(c); 
	    				} 
	    				zin.closeEntry(); 
	    				fout.close(); 
					} catch (Exception e) {
						// ok for now.
						Log.v("Decompress", "Error: " + e.getMessage()); 
					}
    			}
    		} 
    		zin.close(); 
    	} catch(Exception e) { 
    		Log.e("Decompress", "unzip", e); 
    	} 
    } 
     
    private static void dirChecker(String destinationPath) { 
    	File f = new File(destinationPath); 
    	if(!f.isDirectory()) { 
    		f.mkdirs(); 
    	} 
    } 
    
    /**
     * Look for the first .couch file that is not named "welcome.couch"
     * that can be found in the assets folder
     *
     * @return the name of the database (without the .couch extension)
     * @throws IOException
     */
    public String findCouchApp() {
    	String WELCOME_DATABASE = "welcome";	// move to Constants.java
    	String COUCHBASE_DATABASE_SUFFIX  = ".touchdb";	// move to in Constants.java
        String result = null;
        AssetManager assetManager = getAssets();
        String[] assets = null;
        try {
            assets = assetManager.list("");
        } catch (IOException e) {
            Log.e(TAG, "Error listing assets", e);
        }
        if(assets != null) {
            for (String asset : assets) {
                if(!asset.startsWith(WELCOME_DATABASE) && asset.endsWith(COUCHBASE_DATABASE_SUFFIX)) {
                    result = asset.substring(0, asset.length() - COUCHBASE_DATABASE_SUFFIX.length());
                    break;
                }
            }
        }
        return result;
    }
    
	
	  /**
   * kudos: http://stackoverflow.com/a/4530294
   * @param path
   * @param destination
   */
  public static void copyFileOrDir(AssetManager assetManager, String path, String destination) {
      String assets[] = null;
      try {
          assets = assetManager.list(path);
          if (assets.length == 0) {
              copyFile(assetManager, path, destination);
          } else {
              String fullPath = destination + "/" + path;
              File dir = new File(fullPath);
              if (!dir.exists())
                  dir.mkdir();
              for (int i = 0; i < assets.length; ++i) {
                  copyFileOrDir(assetManager, path + File.separator + assets[i], destination);
              }
          }
      } catch (IOException ex) {
          Log.e("tag", "I/O Exception", ex);
      }
  }
  
  private static void copyFile(AssetManager assetManager, String filename, String destination) {
      InputStream in = null;
      OutputStream out = null;
      try {
          in = assetManager.open(filename);
          String newFileName = destination + File.separator + filename;
          out = new FileOutputStream(newFileName);

          byte[] buffer = new byte[1024];
          int read;
          while ((read = in.read(buffer)) != -1) {
              out.write(buffer, 0, read);
          }
          in.close();
          in = null;
          out.flush();
          out.close();
          out = null;
      } catch (Exception e) {
          Log.e("tag", e.getMessage());
      }
  }
    
}
