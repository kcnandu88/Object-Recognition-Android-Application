package org.opencv.objectrecognition;

import android.os.Environment;
import android.util.Log;
import org.opencv.objectrecognition.ContextBean;

public class Variables 
{
 public String getDataDir() {
  try {
   return ContextBean.getLocalContext().getPackageManager().getPackageInfo(
		   ContextBean.getLocalContext().getPackageName(), 0).applicationInfo.dataDir;
  } catch (Exception e) {
   Log.w("Your Tag", "Data Directory error:", e);
   return null;
  }
 }
        // read more about Environment class : http://developer.android.com/reference/android/os/Environment.html
 public String getDownloadFolder() {
  return ContextBean.getLocalContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    .toString();
 }
}