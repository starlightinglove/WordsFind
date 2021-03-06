package com.yorhp.wordfind;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.yorhp.wordfind.utils.FileUtil;
import com.yorhp.wordfind.utils.threadpool.AppExecutors;
import com.yorhp.wordfindlibrary.impl.OcrResult;
import com.yorhp.wordfindlibrary.impl.WordsFindManager;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @author tyhj
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivBg;

    private Bitmap template;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivBg = findViewById(R.id.ivBg);
        try {
            template = BitmapFactory.decodeStream(getAssets().open("test2.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ivBg.setImageBitmap(template);

        requestAllPermissions();
        //初始化工具
        WordsFindManager.getInstance().init(getApplicationContext());


        findViewById(R.id.btnHello).setOnClickListener((v) -> {
            //文字识别
            findAllwords();
        });


        findViewById(R.id.btnFindWords).setOnClickListener((v) -> {
            //找到某一个文字
            findWords("可收取");
        });

    }


    /**
     * 森林界面能量最高的高度
     */
    private static final float START_HEIGHT_TOP_SCALE = 0.23F;

    /**
     * 森林界面能量最低的高度
     */
    private static final float START_HEIGHT_BOTTOM_SCALE = 0.44F;

    /**
     * 找到相应的文字的位置
     * @param words
     */
    private void findWords(String words) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                int height=template.getHeight();
                //裁剪出识别区域
                Bitmap wordsBitmap = FileUtil.cropBitmapY(template, START_HEIGHT_TOP_SCALE, 1 - START_HEIGHT_BOTTOM_SCALE);
                //获取文字所在的区域
                List<Rect> rects = WordsFindManager.getInstance().findWords(wordsBitmap, words);

                if (rects == null || rects.size() == 0) {
                    return;
                }
                //画出文字位置
                Bitmap bitmap1 = wordsBitmap.copy(Bitmap.Config.ARGB_8888, true);
                for (Rect rect : rects) {
                    Log.i("MainActivity", rect.toString());
                    FileUtil.drawRect(bitmap1, rect, Color.argb(150, 180, 52, 217));
                }
                AppExecutors.getInstance().mainThread().execute(() -> {
                    ivBg.setImageBitmap(bitmap1);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 找到所有文字
     */
    private void findAllwords() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //文字识别返回文字内容及位置
                    List<OcrResult> rects = WordsFindManager.getInstance().runModel(template);
                    if (rects == null || rects.size() == 0) {
                        return;
                    }
                    //画出位置来
                    Bitmap bitmap1 = template.copy(Bitmap.Config.ARGB_8888, true);
                    for (OcrResult ocrResult : rects) {
                        Log.i("识别到的文字：",ocrResult.getTxt());
                        FileUtil.drawRect(bitmap1, ocrResult.getRect(), Color.argb(200, 180, 52, 217));
                    }
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        ivBg.setImageBitmap(bitmap1);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 权限请求
     *
     * @return
     */
    private boolean requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    0);
            return false;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        WordsFindManager.getInstance().relese();
        super.onDestroy();
    }
}