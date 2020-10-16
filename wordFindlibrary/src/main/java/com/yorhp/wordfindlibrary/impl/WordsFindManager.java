package com.yorhp.wordfindlibrary.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.yorhp.wordfindlibrary.Predictor;
import com.yorhp.wordfindlibrary.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tyhj
 * @date 2020/10/15
 * @Description: java类作用描述
 */

public class WordsFindManager {

    private static final String modelPath = "models/ocr_v1_for_cpu/ocr_v1_for_cpu";
    private static final String labelPath = "labels/ppocr_keys_v1.txt";
    private static final int cpuThreadNum = 4;
    private static final String cpuPowerMode = "LITE_POWER_HIGH";
    private static final String inputColorFormat = "BGR";
    private static final long[] inputShape = Utils.parseLongsFromString("1,3,960", ",");
    private static final float[] inputMean = Utils.parseFloatsFromString("0.485, 0.456, 0.406", ",");
    private static final float[] inputStd = Utils.parseFloatsFromString("0.229,0.224,0.225", ",");
    private static final float scoreThreshold = 0.1F;

    protected volatile Predictor predictor = new Predictor();

    private Context context;

    private WordsFindManager() {
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        loadModel(context);
        this.context=context;
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static WordsFindManager getInstance() {
        return Holder.instace;
    }



    /**
     * 进行文字识别
     *
     * @param image
     * @return
     */
    public synchronized List<OcrResult> runModel(Bitmap image) {
        if (image != null && predictor.isLoaded()) {
            predictor.setInputImage(image);
            return runModel();
        }
        return null;
    }





    /**
     * 获取图片中的文字
     * @param image
     * @return
     */
    public synchronized List<Rect> findWords(Bitmap image,String words) {
        if (image != null && predictor.isLoaded()) {
            predictor.setInputImage(image);
            List<Rect> rects = new ArrayList<>();
            List<OcrResult> results = runModel();
            if (results != null) {
                for (OcrResult ocrResult : results) {
                    Log.i("WordsFindManager",ocrResult.getTxt());
                    if(ocrResult.getTxt().contains(words)){
                        rects.add(ocrResult.getRect());
                    }
                }
                return rects;
            }
        }
        return null;
    }

    /**
     * 释放资源
     */
    public void relese(){
        predictor.releaseModel();
    }


    private static final class Holder {
        private static final WordsFindManager instace = new WordsFindManager();
    }

    /**
     * 初始化模型
     *
     * @return
     */
    private boolean loadModel(Context context) {
        return predictor.init(context, modelPath, labelPath, cpuThreadNum,
                cpuPowerMode,
                inputColorFormat,
                inputShape, inputMean,
                inputStd, scoreThreshold);
    }

    /**
     * 运行文字识别
     * @return
     */
    private synchronized List<OcrResult> runModel(){
        List<OcrResult> results = predictor.runModel();
        //释放资源
        predictor.releaseModel();
        //重新加载
        loadModel(context);
        return results;
    }


}
