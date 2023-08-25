package com.overthinkers.label;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.airbnb.lottie.LottieAnimationView;

public class splash extends AppCompatActivity {

    LottieAnimationView aV;
    Intent intent;
    Handler isleyici;

    public void splash()
    {
        Runnable beklemeSuresi = new Runnable() {
            @Override
            public void run() {
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        isleyici = new Handler();
        isleyici.postDelayed(beklemeSuresi, 2500);
    }

    public void animasyon()
    {
        aV = findViewById(R.id.lottieV);
        aV.setAnimation("splash.json");
        aV.playAnimation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        animasyon();
        splash();
    }
}