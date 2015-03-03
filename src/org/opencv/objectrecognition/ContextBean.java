/*
 * This will help you to use application context in any file with in project.
 */
package org.opencv.objectrecognition;

import android.content.Context;
public final class ContextBean {
 private static Context localContext;
 public static Context getLocalContext() {
  return localContext;
 }
 public static void setLocalContext(Context _localContext) {
  localContext = _localContext;
 }
}