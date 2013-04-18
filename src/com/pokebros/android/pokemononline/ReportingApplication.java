package com.pokebros.android.pokemononline;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "",
				mailTo = "poscripters@gmail.com",
				mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text,
                customReportContent = { ReportField.PACKAGE_NAME, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.BRAND, ReportField.STACK_TRACE }
				)
public class ReportingApplication extends Application {
	@Override
	public void onCreate() {
        ACRA.init(this);
        super.onCreate();
	}
}
