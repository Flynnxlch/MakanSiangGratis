package com.example.makansianggratis.Model;

public class HistoryItem {
    private String id;
    private String tanggalPinjam;
    private String tanggalKembali;
    private String status;

    public HistoryItem(String id, String tanggalPinjam, String tanggalKembali, String status) {
        this.id = id;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalKembali = tanggalKembali;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTanggalPinjam() {
        return tanggalPinjam;
    }

    public String getTanggalKembali() {
        return tanggalKembali;
    }

    public String getStatus() {
        return status;
    }
}
