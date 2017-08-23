package cry.com.ffmpegkit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import haibao.com.ffmpegkit.FFmpegKit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FFmpegKit.initialize(this, "sfhj");
    }
}
