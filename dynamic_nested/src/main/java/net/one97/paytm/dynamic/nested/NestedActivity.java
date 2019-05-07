package net.one97.paytm.dynamic.nested;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.play.core.splitcompat.SplitCompat;

import org.joda.time.LocalDate;

import java.util.Locale;


public class NestedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.one97.paytm.dynamic.nested.R.layout.activity_nested);
        ((TextView) findViewById(net.one97.paytm.dynamic.nested.R.id.textView)).setText("BM - " + getBirthMonthText((new LocalDate())));
    }

    public String getBirthMonthText(LocalDate dateOfBirth) {
        return dateOfBirth.monthOfYear().getAsText(Locale.ENGLISH);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        SplitCompat.install(this);
    }
}
