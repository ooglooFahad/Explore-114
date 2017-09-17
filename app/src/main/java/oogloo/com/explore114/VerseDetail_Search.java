package oogloo.com.explore114;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VerseDetail_Search extends AppCompatActivity  {

    String verseId, surahId, versetextTR;
    TextView arabic, translation;
    float mRatio = 1.0f;
    final static float STEP = 200;
    int mBaseDist;
    float mBaseRatio;
LinearLayout arabictxt,translationtxt;
    ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_verse_detail__search);
        SpannableString s = new SpannableString("Explore 114");
        s.setSpan(new TypefaceSpan(VerseDetail_Search.this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        //get data from intent
        try {
            verseId = getIntent().getExtras().getString("VerseID");
            surahId = getIntent().getExtras().getString("SurahID");
            versetextTR = getIntent().getExtras().getString("VerseTR");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        arabic = (TextView) findViewById(R.id.tv_arabic);
        arabic.isTextSelectable();
//        arabic.setTextSize(mRatio + 13);
        translation = (TextView) findViewById(R.id.tv_translation);
        translation.isTextSelectable();
//        translation.setTextSize(mRatio + 13);
        GetArabic();
        translation.setText(versetextTR);
        gestureDetector = new GestureDetector(this, new GestureListener());
        scaleGestureDetector =
                new ScaleGestureDetector(this,
                        new MyOnScaleGestureListener(arabic,translation));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return scaleGestureDetector.onTouchEvent(event);
    }

//step 4: add private class GestureListener

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // double tap fired.
            return true;
        }
    }



    public class MyOnScaleGestureListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        TextView arab,trans;
        float factor;

        public MyOnScaleGestureListener(TextView v, TextView iv) {
            super();
            arab = v;
            trans = iv;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
              final float MAX_ZOOM = 150.0f;
            final float MIN_ZOOM = 36.0f;

            float size = arab.getTextSize();
            float size1 = trans.getTextSize();
            Log.d("TextSizeStart", String.valueOf(size));

            float factor = detector.getScaleFactor();
            Log.d("Factor", String.valueOf(factor));


            float product = size*factor;
            float product1 = size1*factor;
            if (product<=MAX_ZOOM && product>=MIN_ZOOM  || product1<=MAX_ZOOM && product1>=MIN_ZOOM )
            {
                Log.d("TextSize", String.valueOf(product));
                arab.setTextSize(TypedValue.COMPLEX_UNIT_PX, product);
                trans.setTextSize(TypedValue.COMPLEX_UNIT_PX, product1);
            }
            size = arab.getTextSize();
            size1 = trans.getTextSize();
            Log.d("TextSizeEnd", String.valueOf(size));
            return true;

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            factor = 1.0f;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    //create option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    //option menu to show translation languages
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.englishTR) {
            GetEnglish();
        }
        if (id == R.id.urduTR) {
            GetUrdu();
        }

        return super.onOptionsItemSelected(item);
    }

    // method to get arabic
    private void GetArabic() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("verseid", verseId);
                params.put("surahid", surahId);
                Network network = new Network("VerseTR_AR.php", params);
                try {

                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);
                        final String str_verseText = positionHashmap.get("verseText");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                arabic.setText(str_verseText);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // method to get translation in english
    private void GetEnglish() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("verseid", verseId);
                params.put("surahid", surahId);
                Network network = new Network("VerseTR_EN.php", params);
                try {

                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);
                        final String str_verseText = positionHashmap.get("verseText");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                translation.setText(str_verseText);//arabic.setText(str_verseText);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // method to get translation in urdu
    private void GetUrdu() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("verseid", verseId);
                params.put("surahid", surahId);
                Network network = new Network("VerseTR_UR.php", params);
                try {

                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);
                        final String str_verseText = positionHashmap.get("verseText");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                translation.setText(str_verseText);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}