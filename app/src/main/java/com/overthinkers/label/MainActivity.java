package com.overthinkers.label;

import static android.provider.MediaStore.Audio.AudioColumns.IS_MUSIC;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MediaPlayer player;
    Timer guncelleyici;
    Button play,onceki,sonraki;
    TextView sarki_ismi_txt, guncel_sure, son_sure;
    SeekBar seek_bar;

    ListView listview;
    ArrayAdapter<String> list_view_adapter;


    int sarki_index = 0;

    ArrayList<String> sarki_list = new ArrayList<>();
    ArrayList<String> sarki_yol_list = new ArrayList<>();

    public void baslangic()
    {
        if (player == null)
        {
            player = MediaPlayer.create(this, Uri.parse(sarki_yol_list.get(sarki_index)));
            bilgiler();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sarki_index = i;
                degistir();
            }
        });

        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    player.seekTo(i);
                    bilgiler();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        }
    }

    public void init() {
        play = findViewById(R.id.play);
        onceki = findViewById(R.id.onceki);
        sonraki = findViewById(R.id.sonraki);
        sarki_ismi_txt = findViewById(R.id.sarki_ismi);
        guncel_sure = findViewById(R.id.guncel_sure);
        son_sure = findViewById(R.id.son_sure);
        listview = findViewById(R.id.listview);
        seek_bar = findViewById(R.id.seek_bar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        dosyalari_al();
    }

    public void play(View view) {
        if (player.isPlaying())
        {
            duraklat();
        }
        else
        {
            oynat();
        }
    }

    public void duraklat() {
        player.pause();
        play.setText("Oynat");
        guncelleyici_durdur();
    }

    public void oynat()
    {
        player.start();
        play.setText("Durdur");
        bilgiler();
    }

    public void onceki_sarki() {
        if (sarki_index > 0) {
            sarki_index--;
        } else {
            Toast.makeText(this, "Baştan başlatıldı", Toast.LENGTH_SHORT).show();
        }
        degistir();
    }

    public void sonraki_sarki() {
        if (sarki_index < sarki_yol_list.size() - 1) {
            sarki_index++;
        } else {
            sarki_index = 0;
            Toast.makeText(this, "Listenin başına dönüldü", Toast.LENGTH_SHORT).show();
        }
        degistir();
    }

    public void degistir() {
        player.stop();
        player.reset();
        player.release();
        player = null;
        player = MediaPlayer.create(this, Uri.parse(sarki_yol_list.get(sarki_index)));
        oynat();
    }

    public void bilgiler() {
        sarki_ismi_txt.setText(sarki_list.get(sarki_index));
        son_sure.setText(String.format("%d:%02d", player.getDuration() / 60000, (player.getDuration() / 1000) % 60));
        seek_bar.setMax(player.getDuration());
        guncelleyici_durdur();
        guncelleyici = new Timer();
        TimerTask guncelleme = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        guncel_sure.setText(String.format("%d:%02d", player.getCurrentPosition() / 60000, (player.getCurrentPosition() / 1000) % 60));
                        seek_bar.setProgress(player.getCurrentPosition());
                        if (guncel_sure.getText().toString().equals(son_sure.getText().toString())) {
                            sonraki_sarki();
                        }
                    }
                });
            }
        };
        guncelleyici.schedule(guncelleme, 0, 1000);
    }

    public void guncelleyici_durdur() {
        if (guncelleyici != null) {
            guncelleyici.cancel();
            guncelleyici = null;
        }
    }

    public void yenile(View view)
    {
        if (sarki_list!=null || sarki_yol_list!=null)
        {
            sarki_list.clear();
            sarki_yol_list.clear();
        }
        dosyalari_al();
    }

    public void sonraki(View view) {sonraki_sarki();}

    public void onceki(View view) {onceki_sarki();}

    @SuppressLint("Range")
    public  void cek()
    {
        String musicName,musicYol;
        Cursor cursor;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        //String[] projection = {MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.DATA};

        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null ,selection, null, null);

        if (cursor.getCount() == 0)
        {
            play.setEnabled(false);
            onceki.setEnabled(false);
            sonraki.setEnabled(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Malesef");
            builder.setMessage("Cihazınızda hiç ses dosyası bulunamadı.");
            builder.setPositiveButton("Uygulamayı kapat", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            while (cursor.moveToNext())
            {
                musicName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                musicYol = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                sarki_list.add(musicName);
                sarki_yol_list.add(musicYol);
            }
            cursor.close();
            list_view_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sarki_list);
            listview.setAdapter(list_view_adapter);
            baslangic();
        }
    }

    public void dosyalari_al()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else
        {
            cek();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                cek();
            }
            else
            {
                play.setEnabled(false);
                onceki.setEnabled(false);
                sonraki.setEnabled(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Malesef");
                builder.setMessage("İzin vermeniz gerekli");
                builder.setPositiveButton("Uygulamayı kapat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

    }
}