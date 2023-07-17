package com.example.absensiapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.absensiapp.databinding.ActivityAbsenBinding;
import com.example.absensiapp.model.Absensi;
import com.example.absensiapp.service.DbHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AbsenActivity extends AppCompatActivity {

    private final int FINE_PERMISSION_CODE = 222;
    private final int CAMERA_PERMISSION_CODE = 100;
    private final int READ_STORAGE_PERMISSION_CODE1 = 1;
    private final int READ_STORAGE_PERMISSION_CODE2 = 2;
    private final int WRITE_STORAGE_PERMISSION_CODE = 3;
    private ActivityAbsenBinding binding;

    private ActivityResultLauncher<Intent> launcherCamera;
    private ActivityResultLauncher<Intent> launcherGalleryDokumen;
    private ActivityResultLauncher<Intent> launcherGalleryKlien;
    private Bitmap photoKlien;
    private Bitmap photoDoc;
    private Bitmap photoRumah;
    private LocationManager locationManager;
    private Location location;
    private String keterangan="";
    private Drive drive;
    private Absensi absensi;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    void initCameraActivity(){
        launcherCamera = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        photoRumah = (Bitmap) result.getData().getExtras().get("data");
                        binding.btnCamera.setImageBitmap(photoRumah);
                    }
                });
    }
    void initGalleryActivity(){
        launcherGalleryDokumen = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        String s = getRealPathFromURI(uri);
                        binding.tvDokumen.setVisibility(View.VISIBLE);
                        binding.tvDokumen.setText(s);
                        try {
                            photoDoc = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            binding.fotoDokumen.setImageBitmap(photoDoc);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });

        launcherGalleryKlien = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        String s = getRealPathFromURI(uri);
                        binding.tvKondisiRumah.setVisibility(View.VISIBLE);
                        binding.tvKondisiRumah.setText(s);

                        try {
                            photoKlien = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            binding.fotoKondisiRumah.setImageBitmap(photoKlien);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
    }
    void initClickListener(){
        binding.btnDokumen.setOnClickListener(view ->{
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_STORAGE_PERMISSION_CODE1);
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE_PERMISSION_CODE);
        });
        binding.btnKondisiRumah.setOnClickListener(view ->{
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_STORAGE_PERMISSION_CODE2);
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE_PERMISSION_CODE);
        });
        binding.btnCamera.setOnClickListener(view -> checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE));
        binding.btnMasuk.setOnClickListener(view -> {
            if (validateField()){
                generatePDF();
            }
        });
        binding.etTglWkt.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            showDatePicker(calendar);
        });
        binding.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            keterangan = ((RadioButton) findViewById(i)).getText().toString();
        });
        binding.etLokasi.setOnClickListener(view -> generateLocation());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbsenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        binding.toolbar.setNavigationOnClickListener(view -> finish());
        initCameraActivity();
        initGalleryActivity();
        initClickListener();
        generateLocation();
        drive = getDriveService(this);
    }

    private void generatePDF() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.layout_pdf,null);
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            this.getDisplay().getRealMetrics(metrics);
            int densityDpi = metrics.densityDpi;
        }else{
            this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(metrics.widthPixels,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(metrics.heightPixels,View.MeasureSpec.EXACTLY));
        view.layout(0,0,metrics.widthPixels,metrics.heightPixels);

        setDataView(view);

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        Bitmap.createScaledBitmap(bitmap, 20, 20, true);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(792, 1120, 2).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0F, 0F, null);
        pdfDocument.finishPage(page);
        File filePath = new File(this.getExternalFilesDir(null), "bitmapPdf.pdf");
        com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
        googleFile.setName("Pasung "+binding.etNama.getText().toString()+ " "+binding.etTglWkt.getText().toString());

        try {
            pdfDocument.writeTo(new FileOutputStream(filePath));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(this,"Uploading...",Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {

                drive.files().create(googleFile, new FileContent("application/pdf",filePath)).execute();
            } catch (IOException  e) {
                throw new RuntimeException(e);
            }
            handler.post(()->{

                Toast.makeText(this,"Insert data success!",Toast.LENGTH_SHORT).show();
                addToDb();
                reset();
            });
        });
        pdfDocument.close();

    }

    private void addToDb() {
        DbHandler.getInstance(this).getDatabase().absensiDAO().insertAbsensi(absensi);
    }

    private void reset() {
        binding.etNamaPendamping.setText("");
        binding.etKabPedamping.setText("");
        binding.etNama.setText("");
        binding.etNik.setText("");
        binding.etTglWkt.setText("");
        binding.tvDokumen.setVisibility(View.GONE);
        binding.tvKondisiRumah.setVisibility(View.GONE);
        binding.btnCamera.setImageDrawable(getResources().getDrawable(R.drawable.baseline_photo_camera_24));
        binding.fotoKondisiRumah.setImageDrawable(getResources().getDrawable(R.drawable.baseline_photo_24));
        binding.fotoDokumen.setImageDrawable(getResources().getDrawable(R.drawable.baseline_photo_24));
    }

    private void setDataView(View view) {
        TextView nama = view.findViewById(R.id.tv_nama);
        TextView nik = view.findViewById(R.id.tv_nik);
        TextView tgl = view.findViewById(R.id.tv_tgl);
        TextView loc = view.findViewById(R.id.tv_lokasi);
        TextView ket = view.findViewById(R.id.tv_ket);
        TextView namaPendamping = view.findViewById(R.id.tv_nama_pendamping);
        TextView kab = view.findViewById(R.id.tv_kab_pendamping);
        TextView ketTambhn = view.findViewById(R.id.tv_ket_tambahan);
        ImageView rmh = view.findViewById(R.id.iv_rumah);
        ImageView doc = view.findViewById(R.id.iv_doc);
        ImageView klien = view.findViewById(R.id.iv_klien);

        String namaStr = binding.etNama.getText().toString();
        String nikStr = binding.etNik.getText().toString();
        String tglStr = binding.etTglWkt.getText().toString();
        String lokasiStr = binding.etLokasi.getText().toString();
        String nama_pedamping = binding.etNamaPendamping.getText().toString();
        String kabupaten = binding.etKabPedamping.getText().toString();
        String txtAreaKet = binding.etKet.getText().toString();
        absensi = new Absensi(namaStr,nikStr,tglStr,keterangan,lokasiStr,photoRumah,photoDoc,photoKlien,nama_pedamping,kabupaten);

        nama.setText("Nama : "+namaStr);
        nik.setText("Nik : "+nikStr);
        tgl.setText("Tgl/Waktu : "+tglStr);
        loc.setText("Lokasi : "+lokasiStr);
        ket.setText("Keterangan : "+keterangan);
        ketTambhn.setText(txtAreaKet);
        namaPendamping.setText("Nama Pendamping : "+nama_pedamping);
        kab.setText("Kabupaten : "+kabupaten);

        rmh.setImageBitmap(photoRumah);
        doc.setImageBitmap(photoDoc);
        klien.setImageBitmap(photoKlien);
    }

    private Drive getDriveService(Context context) {
        Account account = GoogleSignIn.getLastSignedInAccount(context).getAccount();
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account);
        Drive tempDrive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(),credential)
                .setApplicationName(getString(R.string.app_name))
                .build();
        return tempDrive;
    }

    private void showDatePicker(Calendar c){
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AbsenActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("HH:mm");
                    binding.etTglWkt.setText(dayOfMonth+"/"+monthOfYear+"/"+ year1 +" "+simpleDateFormat.format(c.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private boolean validateField() {
        String nama = binding.etNama.getText().toString();
        String nik = binding.etNik.getText().toString();
        String date = binding.etTglWkt.getText().toString();
        String lokasi = binding.etLokasi.getText().toString();
        String nama_pedamping = binding.etNamaPendamping.getText().toString();
        String kabupaten = binding.etKabPedamping.getText().toString();
        String txtAreaKet = binding.etKet.getText().toString();

        if (nama_pedamping.isEmpty()){
            binding.tilNamaPedamping.setError("This field is required!");
            return false;
        }else{
            binding.tilNamaPedamping.setError(null);
        }
        if (kabupaten.isEmpty()){
            binding.tilKabPedamping.setError("This field is required!");
            return false;
        }else{
            binding.tilKabPedamping.setError(null);
        }
        if (nama.isEmpty()){
            binding.tilNama.setError("This field is required!");
            return false;
        }else{
            binding.tilNama.setError(null);
        }
        if (nik.isEmpty()){
            binding.tilNik.setError("This field is required!");
            return false;
        }else{
            binding.tilNik.setError(null);
        }
        if (date.isEmpty()){
            binding.tilTglWkt.setError("This field is required!");
            return false;
        }else{
            binding.tilTglWkt.setError(null);
        }
        if (lokasi.isEmpty()){
            binding.tilLokasi.setError("This field is required!");
            return false;
        }else{
            binding.tilLokasi.setError(null);
        }
        if (keterangan.isEmpty()){
            binding.tvErrorKet.setVisibility(View.VISIBLE);
            return false;
        }else{
            binding.tvErrorKet.setVisibility(View.GONE);
        }

        if (txtAreaKet.isEmpty()){
            binding.tilKet.setError("This field is required!");
            return false;
        }else{
            binding.tilKet.setError(null);
        }


        if (photoDoc == null){
            binding.tvErrorDoc.setVisibility(View.VISIBLE);
            return false;
        }else{
            binding.tvErrorDoc.setVisibility(View.GONE);
        }
        if (photoKlien == null){
            binding.tvErrorRumah.setVisibility(View.VISIBLE);
            return false;
        }else{
            binding.tvErrorRumah.setVisibility(View.GONE);
        }
        if (photoRumah == null){
            binding.tvErrorCamera.setVisibility(View.VISIBLE);
            return false;
        }else{
            binding.tvErrorCamera.setVisibility(View.GONE);
        }
        return true;
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void checkPermission(String permission, int permissionCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permission == Manifest.permission.READ_EXTERNAL_STORAGE){
            if (!Environment.isExternalStorageManager()){
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                catch (Exception e){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }else{
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");

                if (permissionCode == READ_STORAGE_PERMISSION_CODE1){
                    launcherGalleryDokumen.launch(gallery);
                }else{
                    launcherGalleryKlien.launch(gallery);
                };
            }
        }
        else if (ContextCompat.checkSelfPermission(AbsenActivity.this,permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(AbsenActivity.this,new String[]{permission},permissionCode);
        }else{
            if (permissionCode == CAMERA_PERMISSION_CODE){
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                launcherCamera.launch(camera);
            }else if (permissionCode == READ_STORAGE_PERMISSION_CODE1 || permissionCode == READ_STORAGE_PERMISSION_CODE2){
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                if (permissionCode == READ_STORAGE_PERMISSION_CODE1){
                    launcherGalleryDokumen.launch(gallery);
                }else{
                    launcherGalleryKlien.launch(gallery);
                }
            }
        }
    }
    private void generateLocation(){
        if(ContextCompat.checkSelfPermission(AbsenActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(AbsenActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION_CODE);
        }
        if(ContextCompat.checkSelfPermission(AbsenActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (isNetworkEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location!=null){
                getAddress(location.getLatitude(),location.getLongitude());
            }
        }

    }
    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            binding.etLokasi.setText(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}