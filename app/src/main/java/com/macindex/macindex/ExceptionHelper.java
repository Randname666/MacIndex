package com.macindex.macindex;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

class ExceptionHelper {

    public static void handleException(final Context thisContext, final Exception thisException,
                                                 final String exceptionModule, final String exceptionMessage) {
        if (thisContext != null) {
            final String basicInfo = "Generated: " + Calendar.getInstance().getTime() + "\n"
                    + "MacIndex Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n"
                    + "Android Version: " + Build.VERSION.RELEASE + "\n"
                    + "Hardware Model: " + Build.BRAND + " " + Build.MODEL + "\n";

            final String exceptionLog;
            if (exceptionModule != null && exceptionMessage != null) {
                Log.e(exceptionModule, exceptionMessage);
                exceptionLog = "Exception Module: " + exceptionModule + "\n"
                        + "Exception Message: " + exceptionMessage + "\n";
            } else {
                exceptionLog = "Module is not available" + "\n";
            }

            final String exceptionDetails;
            if (thisException == null) {
                exceptionDetails = "Detail is not available" + "\n";
            } else {
                thisException.printStackTrace();
                exceptionDetails = "Exception Details:" + "\n" + getStackTrace(thisException);
            }

            handleExceptionDialog(thisContext, basicInfo + exceptionLog + exceptionDetails);
        }
    }

    private static void handleExceptionDialog(final Context thisContext, final String exceptionInfo) {
        final AlertDialog.Builder exceptionDialog = new AlertDialog.Builder(thisContext);
        exceptionDialog.setTitle(R.string.error);
        exceptionDialog.setMessage(R.string.error_information);
        exceptionDialog.setCancelable(false);
        exceptionDialog.setPositiveButton(R.string.error_dismiss, (dialogInterface, i) -> {
            PrefsHelper.triggerRebirth(thisContext);
        });
        exceptionDialog.setNegativeButton(R.string.error_copy_button, (dialogInterface, i) -> {
        });

        final View infoChunk = ((LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chunk_exception_dialog, null);
        final TextView exceptionInfoBox = infoChunk.findViewById(R.id.exceptionInfo);

        exceptionInfoBox.setText(exceptionInfo);
        exceptionDialog.setView(infoChunk);

        final AlertDialog exceptionDialogCreated = exceptionDialog.create();
        exceptionDialogCreated.show();

        // Override the negative button
        exceptionDialogCreated.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) thisContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ExceptionInfo", exceptionInfo);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(thisContext,
                    MainActivity.getRes().getString(R.string.error_copy_information), Toast.LENGTH_LONG).show();
        });
    }

    private static String getStackTrace(final Exception thisException) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        thisException.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
