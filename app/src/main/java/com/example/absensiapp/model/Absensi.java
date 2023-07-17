package com.example.absensiapp.model;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Absensi {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "id_pedamping")
    private String namaPendamping;
    @ColumnInfo(name = "kabupaten")
    private String kabupaten;
    @ColumnInfo(name = "nama")
    private String nama;
    @ColumnInfo(name = "nik")
    private String nik;
    @ColumnInfo(name = "tglWktu")
    private String tglWkt;
    @ColumnInfo(name = "keterangan")
    private String ket;
    @ColumnInfo(name = "lokasi")
    private String lokasi;
    @ColumnInfo(name = "fto1")
    private Bitmap fto1;
    @ColumnInfo(name = "fto2")
    private Bitmap fto2;
    @ColumnInfo(name = "fto3")
    private Bitmap fto3;

    public Absensi(String nama, String nik, String tglWkt, String ket, String lokasi,
                   Bitmap fto1, Bitmap fto2, Bitmap fto3,String namaPendamping,String kabupaten) {
        this.nama = nama;
        this.nik = nik;
        this.tglWkt = tglWkt;
        this.ket = ket;
        this.lokasi = lokasi;
        this.fto1 = fto1;
        this.fto2 = fto2;
        this.fto3 = fto3;
        this.namaPendamping = namaPendamping;
        this.kabupaten = kabupaten;
    }

    public String getNamaPendamping() {
        return namaPendamping;
    }

    public void setNamaPendamping(String namaPendamping) {
        this.namaPendamping = namaPendamping;
    }

    public String getKabupaten() {
        return kabupaten;
    }

    public void setKabupaten(String kabupaten) {
        this.kabupaten = kabupaten;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getTglWkt() {
        return tglWkt;
    }

    public void setTglWkt(String tglWkt) {
        this.tglWkt = tglWkt;
    }

    public String getKet() {
        return ket;
    }

    public void setKet(String ket) {
        this.ket = ket;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public Bitmap getFto1() {
        return fto1;
    }

    public void setFto1(Bitmap fto1) {
        this.fto1 = fto1;
    }

    public Bitmap getFto2() {
        return fto2;
    }

    public void setFto2(Bitmap fto2) {
        this.fto2 = fto2;
    }

    public Bitmap getFto3() {
        return fto3;
    }

    public void setFto3(Bitmap fto3) {
        this.fto3 = fto3;
    }
}
