package oogloo.com.explore114;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

public class Setting extends AppCompatActivity {

    TextView settingtext, tv, selected;
    RadioGroup rg;
    RadioButton rb_en, rb_ur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_setting);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        setTitleColor(R.color.colorAccent);
        SpannableString s = new SpannableString("SETTINGS");
        s.setSpan(new TypefaceSpan(this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Bariol_Regular.otf");
        //initialize
        rg = (RadioGroup) findViewById(R.id.rg);
        rb_en = (RadioButton) findViewById(R.id.rb_eng);
        rb_ur = (RadioButton) findViewById(R.id.rb_urd);
        settingtext = (TextView) findViewById(R.id.settingtext);
        settingtext.setTypeface(custom_font);
        selected = (TextView) findViewById(R.id.selected);
        selected.setSelected(true);
        selected.setTypeface(custom_font);
        tv = (TextView) findViewById(R.id.tv);
        tv.setSelected(true);
        tv.setTypeface(custom_font);
        //get language name and set radio button
        SharedPreferences settings = getSharedPreferences("language", 0);
        String lang = settings.getString("lang", "urdu");
        if (lang.equalsIgnoreCase("urdu")) {
            rb_ur.setChecked(true);
            settingtext.setText("URDU");
        } else {
            rb_en.setChecked(true);
            settingtext.setText("ENGLISH");
        }
//store language in shared prefrences
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonID = group.getCheckedRadioButtonId();
                View radio = group.findViewById(radioButtonID);
                int pos = group.indexOfChild(radio);
                RadioButton btn = (RadioButton) rg.getChildAt(pos);
                String selection = (String) btn.getText().toString();
                SharedPreferences settings = getSharedPreferences("language", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("lang", selection);
                editor.commit();
                Toast.makeText(Setting.this, selection, Toast.LENGTH_SHORT).show();
                settingtext.setText(selection);
            }
        });
    }

    public static class TypefaceUtil {

        /**
         * Using reflection to override default typeface
         * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
         *
         * @param context                    to work with assets
         * @param defaultFontNameToOverride  for example "monospace"
         * @param customFontFileNameInAssets file name of the font from assets
         */
        public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
            try {
                final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);

                final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
                defaultFontTypefaceField.setAccessible(true);
                defaultFontTypefaceField.set(null, customFontTypeface);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
