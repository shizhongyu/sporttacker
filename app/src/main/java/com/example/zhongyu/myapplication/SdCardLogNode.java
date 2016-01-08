package com.example.zhongyu.myapplication;
/*
 * @description
 *   Please write the SdCardLogNode module's description
 * @author Zhang (rdshoep@126.com)
 *   http://www.rdshoep.com/
 * @version 
 *   1.0.0(1/6/2016)
 */

import android.content.Context;

import com.tulipsport.android.common.logger.LogNode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SdCardLogNode implements LogNode {
    boolean status = false;
    private Context context;
    private Map<String, StringBuffer> _logContainer;

    public SdCardLogNode(Context context) {
        this(context, new HashMap<String, StringBuffer>());
    }

    public SdCardLogNode(Context context, Map<String, StringBuffer> _logContainer) {
        this.context = context;
        this._logContainer = _logContainer;
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        if (!status) return;

        StringBuffer stringBuffer = _logContainer.get(tag);

        if (stringBuffer == null) {
            stringBuffer = new StringBuffer();
            _logContainer.put(tag, stringBuffer);
        }

        stringBuffer.append(msg + "\r\n");
    }

    public void start() {
        status = true;
    }

    public void stop() {
        status = false;
    }

    public void clearTag(String tag) {
        _logContainer.put(tag, null);
    }

    public void flush(String tag, String name) {
        StringBuffer stringBuffer = _logContainer.get(tag);
        if (stringBuffer != null) {
            String allLogs = stringBuffer.toString();
            write(context, name + ".txt", allLogs);
            clearTag(tag);
        }
    }

    /**
     * 写文本文件
     *
     * @param context
     * @param
     */
    public static void write(Context context, String fileName, String content) {
        if (content == null)
            content = "";

        try {
            File cacheFolder = context.getExternalCacheDir();
            String filePath = cacheFolder.getAbsolutePath() + File.separator + fileName;
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);

            fos.write(content.getBytes());

            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
