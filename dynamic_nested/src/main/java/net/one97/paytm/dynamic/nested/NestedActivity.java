package net.one97.paytm.dynamic.nested;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class NestedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested);
        Picasso.get().load(Uri.parse("https://media.licdn.com/dms/image/C4D03AQHUfGSTb4FMSA/profile-displayphoto-shrink_200_200/0?e=1562803200&v=beta&t=JQDYuVXAP-x0ebxN1YGd4j3OqtGZTLBoFkQ4VgYRetM")).into((ImageView) findViewById(R.id.imageView));
    }
}
