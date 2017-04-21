package com.don.democatchhandler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DON on 17/04/21.
 */

public class CrashHandler implements UncaughtExceptionHandler {

  private static final String TAG = "CrashHandler";
  private static CrashHandler INSTANCE;
  private static final boolean isUserHandler = true;
  private Context mContext;
  private UncaughtExceptionHandler mDefaultHandler;
  private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
  // 用来存储设备信息和异常信息
  private Map<String, String> infos = new HashMap<String, String>();
  private String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash/";
  private String fileNameFormat = "crash-%s-%d.log";

  private CrashHandler() {
  }

  static {
    INSTANCE = new CrashHandler();
  }

  /**
   * 单例模式，获取自定义异常处理类
   */
  public static CrashHandler getInstance() {
    return INSTANCE;
  }

  public void init(Context context) {
    mContext = context;
    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    if (isUserHandler) {
      ex.printStackTrace();

      new Thread() {
        @Override
        public void run() {
          Looper.prepare();
          Toast.makeText(mContext, "sorry crash...", Toast.LENGTH_LONG).show();
          Looper.loop();
        }
      }.start();

      // 收集设备参数信息
      collectDeviceInfo(mContext);
      // 保存日志文件
      saveCrashInfo2File(ex);

      /**
       * 以后要加上传功能,需要启动服务去上传应该
       */

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(0);
    } else {
      // 如果用户没有处理则让系统默认的异常处理器来处理
      mDefaultHandler.uncaughtException(thread, ex);
    }
  }

  /**
   * 收集设备参数信息
   */
  public void collectDeviceInfo(Context ctx) {
    try {
      PackageManager pm = ctx.getPackageManager();
      PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
          PackageManager.GET_ACTIVITIES);
      if (pi != null) {
        String versionName = pi.versionName == null ? "null"
            : pi.versionName;
        String versionCode = pi.versionCode + "";
        infos.put("versionName", versionName);
        infos.put("versionCode", versionCode);
      }
    } catch (NameNotFoundException e) {
      Log.e(TAG, "an error occured when collect package info", e);
    }
    Field[] fields = Build.class.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        infos.put(field.getName(), field.get(null).toString());
        Log.d(TAG, field.getName() + " : " + field.get(null));
      } catch (Exception e) {
        Log.e(TAG, "an error occured when collect crash info", e);
      }
    }
  }

  /**
   * 保存错误信息到文件中
   *
   * @return 返回文件名称, 便于将文件传送到服务器
   */
  private String saveCrashInfo2File(Throwable ex) {

    StringBuffer sb = new StringBuffer();
    for (Map.Entry<String, String> entry : infos.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      sb.append(key + "=" + value + "\n");
    }

    Writer writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    ex.printStackTrace(printWriter);
    Throwable cause = ex.getCause();
    while (cause != null) {
      cause.printStackTrace(printWriter);
      cause = cause.getCause();
    }
    printWriter.close();
    String result = writer.toString();
    sb.append(result);
    try {
      long timestamp = System.currentTimeMillis();
      String time = formatter.format(new Date());
      String fileName = String.format(fileNameFormat, time, timestamp);
      if (Environment.getExternalStorageState().equals(
          Environment.MEDIA_MOUNTED)) {
        String path = savePath;
        File dir = new File(path);
        if (!dir.exists()) {
          dir.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(path + fileName);
        fos.write(sb.toString().getBytes());
        fos.close();
      }
      return fileName;
    } catch (Exception e) {
      Log.e(TAG, "an error occured while writing file...", e);
    }
    return null;
  }
}
