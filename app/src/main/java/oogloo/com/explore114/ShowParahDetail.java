package oogloo.com.explore114;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ShowParahDetail extends AppCompatActivity {
    ProgressDialog pd;
    CustomAdapter c;
    ArrayList<Ayah_textAR> ar_ayahAR;
    ArrayList<Ayah_textTR> ar_ayah_TR;
    ListView lv;
    String intent_parah_ID;
    String intent_parahName;
    int topp, indexx;
    int statt, toppp;
    ShareDialog shareDialog;
    Context context = this;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        facebookSDKInitialize();
        setContentView(R.layout.activity_show_parah_detail);
        shareDialog = new ShareDialog(ShowParahDetail.this);
        intent_parah_ID = getIntent().getExtras().getString("PS_ID");
        intent_parahName = getIntent().getExtras().getString("PS_Name");
        statt = getIntent().getExtras().getInt("statt", 0);
        toppp = getIntent().getExtras().getInt("toppp", 0);

        SpannableString s = new SpannableString(intent_parahName);
        s.setSpan(new TypefaceSpan(ShowParahDetail.this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        pd = new ProgressDialog(ShowParahDetail.this, R.style.pdtheme);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
        lv = (ListView) findViewById(R.id.lvparahDetail);
        ar_ayahAR = new ArrayList<>();
        ar_ayah_TR = new ArrayList<>();

        SharedPreferences settings = getSharedPreferences("language", 0);
        String lang = settings.getString("lang", "urdu"); //urdu is the default value
        if (!isNetworkAvailable()) {
            show_alert();
        } else {
            if (lang.equals("ENGLISH")) {
                ShowDataAR();
                ShowTranslationEN();
            } else {
                ShowDataAR();
                ShowTranslationUR();
            }
        }
        isPermissionGranted();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.englishTR) {
            if (!isNetworkAvailable()) {
                show_alert();
            }
            ar_ayahAR.clear();
            ar_ayah_TR.clear();
            ShowDataAR();
            ShowTranslationEN();
        }
        if (id == R.id.urduTR) {
            if (!isNetworkAvailable()) {
                show_alert();
            }
            ar_ayahAR.clear();
            ar_ayah_TR.clear();
            ShowDataAR();
            ShowTranslationUR();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {

                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(ShowParahDetail.this, "Now you cant share on Whatsapp", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void ShowDataAR() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("parahid", intent_parah_ID);
                Network network = new Network("showParahDetails.php", params);

                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);
                        String str_ayahID = positionHashmap.get("verse_id");
                        String str_parahID = positionHashmap.get("parah_id");
                        String str_ayahText = positionHashmap.get("verseText");

                        Ayah_textAR ayahText = new Ayah_textAR();
                        ayahText.ayah_id = str_ayahID;
                        ayahText.parah_id = str_parahID;
                        ayahText.ayah_textAR = str_ayahText;
                        ar_ayahAR.add(ayahText);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ShowParahDetail.this == null)
                        return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c = new CustomAdapter(ShowParahDetail.this, R.layout.cstm_parah, R.id.tv_parahNAME, ar_ayahAR);
                            lv.setAdapter(c);
                            if (statt != 0) {
                                lv.setSelectionFromTop(statt, toppp);
                            }
                            if (pd != null) {
                                pd.dismiss();
                                pd = null;
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void ShowTranslationEN() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("parahid", intent_parah_ID);
                Network network = new Network("showParahTranslationEN.php", params);

                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);

                        String str_ayahText = positionHashmap.get("verseText");
                        String str_verseID = positionHashmap.get("verse_id");
                        String str_surahID = positionHashmap.get("surah_id");


                        Ayah_textTR ayahTR = new Ayah_textTR();
                        ayahTR.ayah_textTR = str_ayahText;
                        ayahTR.verse_id = str_verseID;
                        ayahTR.surah_id = str_surahID;
                        ar_ayah_TR.add(ayahTR);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void ShowTranslationUR() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("parahid", intent_parah_ID);
                Network network = new Network("showParahTranslationUR.php", params);

                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);

                        String str_ayahText = positionHashmap.get("verseText");
                        String str_verseID = positionHashmap.get("verse_id");
                        String str_surahID = positionHashmap.get("surah_id");


                        Ayah_textTR ayahTR = new Ayah_textTR();
                        ayahTR.ayah_textTR = str_ayahText;
                        ayahTR.verse_id = str_verseID;
                        ayahTR.surah_id = str_surahID;
                        ar_ayah_TR.add(ayahTR);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class CustomAdapter extends ArrayAdapter<Ayah_textAR> {
        public CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<Ayah_textAR> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.cstm_ayah_full, parent, false);
            final RelativeLayout top = (RelativeLayout) v.findViewById(R.id.rel_top);
            final LinearLayout lv_icon = (LinearLayout) v.findViewById(R.id.lv_icon);

            Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");

            final ImageView options = (ImageView) v.findViewById(R.id.options);
            final TextView tv_ayah_text = (TextView) v.findViewById(R.id.tv_ayahText);
            final TextView tv_ayahTR = (TextView) v.findViewById(R.id.tv_ayahTR);
            tv_ayahTR.setTypeface(custom_font);
            final ImageView bookmark = (ImageView) v.findViewById(R.id.bookmark);
            final ImageView delete_bookmark = (ImageView) v.findViewById(R.id.delete_bookmark);
            try {
                Ayah_textAR ayah = ar_ayahAR.get(position);
                tv_ayah_text.setText(ayah.ayah_id + " : " + ayah.ayah_textAR);
                if (ayah.ayah_id.equals("1"))
                    tv_ayah_text.setTextColor(Color.WHITE);
                Ayah_textTR parahTR = ar_ayah_TR.get(position);
                tv_ayahTR.setText(parahTR.ayah_textTR);

                SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                Cursor c = db.rawQuery("select * from bookmarks ", null);
                try {
                    while (c.moveToNext()) {
                        String verseid = c.getString(0);
                        String surah_id = c.getString(1);
                        if (verseid.equals(ayah.ayah_id) && surah_id.equals(parahTR.surah_id)) {
                            bookmark.setVisibility(View.GONE);
                            delete_bookmark.setVisibility(View.VISIBLE);
                        }
                    }
                } finally {
                    c.close();
                }
            } catch (Exception e) {
                notifyDataSetChanged();
            }
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    try {
                        switch (v.getId()) {
                            case R.id.options:

                                PopupMenu popup = new PopupMenu(ShowParahDetail.this, v);
                                popup.getMenuInflater().inflate(R.menu.popupmenu,
                                        popup.getMenu());
                                popup.show();
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()) {
                                            case R.id.popupdetail:

                                                try {
                                                    Ayah_textTR search_verse = ar_ayah_TR.get(position);
                                                    Intent i = new Intent(ShowParahDetail.this, VerseDetail_Search.class);
                                                    i.putExtra("VerseTR", search_verse.ayah_textTR);
                                                    i.putExtra("VerseID", search_verse.verse_id);
                                                    i.putExtra("SurahID", search_verse.surah_id);
                                                    startActivity(i);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                break;
                                            case R.id.fb:
                                                try {
                                                    options.setVisibility(View.GONE);
                                                    bookmark.setVisibility(View.GONE);
                                                    delete_bookmark.setVisibility(View.GONE);
                                                    lv_icon.setVisibility(View.VISIBLE);
                                                    Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.backgr);
                                                    Bitmap bitmap = generateBitmap(top);
                                                    Bitmap finalpic = makepakpic(background, bitmap);
                                                    lv_icon.setVisibility(View.GONE);
                                                    ShareDialog(finalpic);

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.whatsapp:
                                                try {
                                                    options.setVisibility(View.GONE);
                                                    bookmark.setVisibility(View.GONE);
                                                    delete_bookmark.setVisibility(View.GONE);
                                                    lv_icon.setVisibility(View.VISIBLE);
                                                    Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.backgr);
                                                    Bitmap bitmap = generateBitmap(top);
                                                    Bitmap finalpic = makepakpic(background, bitmap);
                                                    lv_icon.setVisibility(View.GONE);

                                                    Uri uri = getImageUri(v.getContext(), finalpic);
                                                    String Text = "Get this Quran app for your Android phone from Playstore:" + "\n" + "https://play.google.com/store/apps/details?id=oogloo.com.explore114";

                                                    Intent shareIntent = new Intent();
                                                    shareIntent.setAction(Intent.ACTION_SEND);
                                                    //Target whatsapp:
                                                    shareIntent.setPackage("com.whatsapp");
                                                    //Add text and then Image URI
                                                    shareIntent.putExtra(Intent.EXTRA_TEXT, Text);
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                                    shareIntent.setType("image/jpeg");
                                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                    startActivity(shareIntent);

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.others:
                                                try {


                                                    Ayah_textAR ayah = ar_ayahAR.get(position);
                                                    Ayah_textTR ayah2 = ar_ayah_TR.get(position);
                                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                                    sharingIntent.setType("text/plain");
                                                    Uri uri = Uri.parse(tv_ayah_text.getText().toString() + "\n" + tv_ayahTR.getText().toString() + "\n"
                                                            + "Parah No : " + ayah.parah_id + " Surah No : " + ayah2.surah_id + " Verse No : " + ayah2.verse_id + "\n\n"
                                                            + "Explore 114" + "\n" + "OOGLOO Web and Beyond");
                                                    String shareBody = " " + uri;
                                                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "EXPLORE 114");
                                                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        return true;
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Ayah_textAR ayah_textAR = ar_ayahAR.get(position);
                        Ayah_textTR ayah_textTR = ar_ayah_TR.get(position);
                        final SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                        db.execSQL("create table if not exists bookmarks(verse_id INTEGER,surah_id INTEGER,verse_textAR TEXT);");
                        //database insert
                        db.execSQL("insert into bookmarks values('" + ayah_textTR.verse_id + "', '" + ayah_textTR.surah_id + "'," +
                                "'" + ayah_textAR.ayah_textAR + "');");
                        delete_bookmark.setVisibility(View.VISIBLE);
                        Toast.makeText(ShowParahDetail.this, "BOOKMARK SAVED", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            delete_bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Ayah_textTR ayah_textTR = ar_ayah_TR.get(position);
                        SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                        db.execSQL("DELETE FROM bookmarks WHERE verse_id = '" + ayah_textTR.verse_id + "' AND surah_id = '" + ayah_textTR.surah_id + "';");
                        db.close();
                        bookmark.setVisibility(View.VISIBLE);
                        delete_bookmark.setVisibility(View.GONE);
                        Toast.makeText(ShowParahDetail.this, "BOOKMARK REMOVED", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return v;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap makepakpic(Bitmap firstImage, Bitmap secondImage) {

        int width = secondImage.getWidth();
        int height = secondImage.getHeight();
        firstImage = Bitmap.createScaledBitmap(
                firstImage, width, height, false);
        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0, 0, null);
        canvas.drawBitmap(secondImage, 0, 0, null);
        return result;
    }

    protected void facebookSDKInitialize() {

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
    }

    public void ShareDialog(Bitmap imagePath) {

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(imagePath)
                .setCaption("EXPLORE 114")
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        shareDialog.show(content);
    }

    private Bitmap generateBitmap(RelativeLayout v) {

        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(),
                v.getHeight()
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    public class Ayah_textAR {
        String ayah_id;
        String parah_id;
        String ayah_textAR;
    }

    public class Ayah_textTR {
        String verse_id;
        String ayah_textTR;
        String surah_id;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    private void show_alert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(ShowParahDetail.this, R.style.mydialog);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            alert.setMessage(Html.fromHtml("<b>" + "PLEASE CONNECT TO INTERNET!" + "</b>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            alert.setMessage(Html.fromHtml("<b>" + "PLEASE CONNECT TO INTERNET!" + "</b>"));
        }
        alert.setCancelable(false);
        alert.setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(getIntent());
            }
        });
        alert.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();

        Button b_pos;
        b_pos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (b_pos != null) {
            b_pos.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        Button b_neg;
        b_neg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (b_neg != null) {
            b_neg.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        indexx = lv.getFirstVisiblePosition();
        View v = lv.getChildAt(0);
        topp = (v == null) ? 0 : (v.getTop() - lv.getPaddingTop());
        SharedPreferences seting = getSharedPreferences("lvstate", 0);
        SharedPreferences.Editor editor = seting.edit();
        editor.putInt("indexx", indexx);
        editor.putInt("topp", topp);
        editor.putString("parahID", intent_parah_ID);
        editor.putString("parahNAME", intent_parahName);
        editor.putString("find_activity", "Parah");
        editor.commit();
        super.onBackPressed();
    }
}


//                        options_rv.animate()
//                                .alpha(0.0f)
//                                .setDuration(300)
//                                .setListener(new AnimatorListenerAdapter() {
//                                    @Override
//                                    public void onAnimationEnd(Animator animation) {
//                                        super.onAnimationEnd(animation);
//                                        options_rv.setVisibility(View.GONE);
//                                    }
//                                });
//                        options.animate().setListener(null);
//                        options_rv.setVisibility(View.GONE);


//                        options_rv.animate()
//                                .alpha(0.0f)
//                                .setDuration(300)
//                                .setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
//                            @Override
//                            public void run() {
//                                options_rv.animate().alpha(1.0f).setDuration(300).setInterpolator(new AccelerateInterpolator()).start();
//                            }
//                        })
//                                .setListener(new AnimatorListenerAdapter() {
//                                    @Override
//                                    public void onAnimationEnd(Animator animation) {
//                                        super.onAnimationEnd(animation);
//                                        options_rv.setVisibility(View.VISIBLE);
//                                    }
//                                });
//                        options_rv.animate()
//                                .alpha(1.0f)
//                                .setDuration(300)
//                                .setListener(new AnimatorListenerAdapter() {
//                                    @Override
//                                    public void onAnimationEnd(Animator animation) {
//                                        super.onAnimationEnd(animation);
//                                        options_rv.setVisibility(View.VISIBLE);
//                                    }
//                                });
//                        options.animate().setListener(null);