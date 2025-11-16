package com.example.ejercicio33;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button btnGrabar, btnGuardar, btnCancelar;
    private EditText edtNombre;
    private TextView txtTimer;
    private RecyclerView recyclerRecordings;
    private AudioDbHelper dbHelper;
    private RecordingAdapter adapter;
    private List<AudioRecording> recordingsList;
    private MediaRecorder mediaRecorder;
    private String tempFilePath;
    private long recordingStartTime;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa vistas
        btnGrabar = findViewById(R.id.btnGrabar);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
        edtNombre = findViewById(R.id.edtNombre);
        txtTimer = findViewById(R.id.txtTimer);
        recyclerRecordings = findViewById(R.id.recyclerRecordings);

        dbHelper = new AudioDbHelper(this);
        recordingsList = dbHelper.getAllRecordings();
        Log.d("MainActivity", "btnGrabar: " + btnGrabar);
        adapter = new RecordingAdapter(recordingsList, new RecordingAdapter.OnItemActionListener() {
            @Override
            public void onPlay(AudioRecording rec) {
                playRecording(rec.id);
            }
            @Override
            public void onDelete(AudioRecording rec) {
                dbHelper.deleteRecording(rec.id);
                refreshList();
            }
        });
        recyclerRecordings.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecordings.setAdapter(adapter);

        btnGrabar.setOnClickListener(v -> startRecording());
        btnGuardar.setOnClickListener(v -> stopAndSaveRecording());
        btnCancelar.setOnClickListener(v -> cancelRecording());
    }
    private void refreshList() {
        recordingsList.clear();
        recordingsList.addAll(dbHelper.getAllRecordings());
        adapter.notifyDataSetChanged();
    }
    private void startRecording() {
        // Solicita permisos si es necesario
        if (!hasAudioPermission()) {
            requestAudioPermission();
            return;
        }

        tempFilePath = getExternalFilesDir(null) + "/temp_rec.m4a";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(tempFilePath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            txtTimer.setVisibility(View.VISIBLE);
            runTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void runTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsed = (System.currentTimeMillis() - recordingStartTime) / 1000;
                    txtTimer.setText(String.format("%02d:%02d", elapsed / 60, elapsed % 60));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }
    private void stopAndSaveRecording() {
        if (!isRecording) return;
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        timerHandler.removeCallbacks(timerRunnable);
        txtTimer.setVisibility(View.GONE);
        // Guarda audio a BD
        File file = new File(tempFilePath);
        byte[] audioBytes = fileToBytes(file);
        String nombre = edtNombre.getText().toString().trim();
        int duracion = (int)((System.currentTimeMillis() - recordingStartTime) / 1000);
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        dbHelper.insertRecording(nombre, audioBytes, duracion, fecha);
        refreshList();
        file.delete(); // Borra temporal
    }
    private byte[] fileToBytes(File file) {
        try (FileInputStream fis = new FileInputStream(file); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096]; int len;
            while ((len = fis.read(buf)) > 0) bos.write(buf, 0, len);
            return bos.toByteArray();
        } catch (Exception e) { return null; }
    }
    private void cancelRecording() {
        if (isRecording && mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            timerHandler.removeCallbacks(timerRunnable);
            txtTimer.setVisibility(View.GONE);
            File file = new File(tempFilePath);
            if (file.exists()) file.delete();
        }
    }
    private void playRecording(long id) {
        byte[] audioBytes = dbHelper.getAudioDataById(id);
        try {
            File temp = File.createTempFile("play_", ".m4a", getCacheDir());
            FileOutputStream fos = new FileOutputStream(temp);
            fos.write(audioBytes); fos.close();
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(temp.getAbsolutePath());
            mp.prepare();
            mp.start();
            // puedes manejar pausa/detener agregando referencias
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Métodos de permisos:
    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{ Manifest.permission.RECORD_AUDIO },
                100
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                startRecording(); // Intenta grabar de nuevo
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
