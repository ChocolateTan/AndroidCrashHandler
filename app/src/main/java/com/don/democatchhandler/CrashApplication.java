package com.don.democatchhandler;

import android.app.Application;

/**
 * Created by DON on 17/04/21.
 */

public class CrashApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    CrashHandler.getInstance().init(this);
  }
}
