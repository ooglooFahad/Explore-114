package oogloo.com.explore114;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class Search extends AppCompatActivity {

    Context context=this;
    RadioGroup rg_main, rg_verse;
    Spinner surah_name;
    TextView defaulty_lan,search_by,search_by_verse;
    EditText et_verse, et_search;
    String size="  SEARCH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_serach);
        SpannableString s = new SpannableString(size);
        s.setSpan(new TypefaceSpan(this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.search_icon);
//        rg_main = (RadioGroup) findViewById(R.id.rg_main);
//        rg_verse = (RadioGroup) findViewById(R.id.rg_verse);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Bariol_Regular.otf");
        defaulty_lan=(TextView)findViewById(R.id.defaulty_lan);
        defaulty_lan.setSelected(true);
        defaulty_lan.setTypeface(custom_font);
        search_by=(TextView) findViewById(R.id.search_by);
        search_by.setSelected(true);
        search_by.setTypeface(custom_font);
        search_by_verse=(TextView) findViewById(R.id.search_by_verse);
        search_by_verse.setSelected(true);
        search_by_verse.setTypeface(custom_font);
        et_verse = (EditText) findViewById(R.id.et_verse);
        et_verse.setMovementMethod(null);
        et_verse.setTypeface(custom_font);
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.setMovementMethod(null);
        et_search.setTypeface(custom_font);

        final Spinner spinner_lang = (Spinner) findViewById(R.id.spinner_lang);
        spinner_lang.setPopupBackgroundResource(R.color.spinner_deopdown_background);

        MySpinnerAdapter adapter = new MySpinnerAdapter(
                context,R.layout.spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.spinner_lang)));
//        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.spinner_lang, R.layout.spinner_item);
        spinner_lang.setAdapter(adapter);

        final Spinner surah_name = (Spinner) findViewById(R.id.spiner_surah);
        surah_name.setPopupBackgroundResource(R.color.spinner_deopdown_background);
        MySpinnerAdapter adapter_parah = new MySpinnerAdapter(
                context,R.layout.spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.spinnerItems)));
//        ArrayAdapter adapter_parah = ArrayAdapter.createFromResource(this, R.array.spinnerItems, R.layout.spinner_item);
        surah_name.setAdapter(adapter_parah);


//search by verse number
        et_verse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    final int s_name=Integer.parseInt(surah_name.getSelectedItem().toString().replaceAll("[\\D]", ""));
                    if (et_verse == null || et_verse.length() == 0 || et_verse.getText().toString().equals("0")) {
                        et_verse.setError("Invalid Verse No.");
                    } else {
                        Intent i = new Intent(Search.this, SearchResult.class);
                        i.putExtra("verseID", et_verse.getText().toString());
                        i.putExtra("surahID", s_name+"");
                        startActivity(i);
                        return true;
                    }
                }
                return false;
            }
        });
        //search by word
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

//                    int radioButtonID = rg_verse.getCheckedRadioButtonId();
//                    View radio = rg_verse.findViewById(radioButtonID);
//                    int pos = rg_verse.indexOfChild(radio);
//                    RadioButton btn = (RadioButton) rg_verse.getChildAt(pos);
                    String selection = spinner_lang.getSelectedItem().toString();
                            //(String) btn.getText().toString();
                    if (et_search == null || et_search.length() == 0) {
                        et_search.setError("Enter Word To Search ");
                    } else {
                        if (selection.equalsIgnoreCase("english")) {
                            Intent i = new Intent(Search.this, SearchWordEN.class);
                            i.putExtra("word", et_search.getText().toString());
                            startActivity(i);
                        } else if (selection.equalsIgnoreCase("urdu")) {
                            Intent i = new Intent(Search.this, SearchWordUR.class);
                            i.putExtra("word", et_search.getText().toString());
                            startActivity(i);
                        } else {
                            Intent i = new Intent(Search.this, SearchWordAR.class);
                            i.putExtra("word", et_search.getText().toString());
                            startActivity(i);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

    }
}
