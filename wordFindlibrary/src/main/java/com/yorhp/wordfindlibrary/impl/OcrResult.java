package com.yorhp.wordfindlibrary.impl;

import android.graphics.Rect;

/**
 * @author tyhj
 * @date 2020/10/15
 * @Description: java类作用描述
 */

public class OcrResult {

    /**
     * 识别出来的文字
     */
    private String txt;

    /**
     * 文字所在的位置
     */
    private Rect rect;

    /**
     * 可信度
     */
    private float confidence;


    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
