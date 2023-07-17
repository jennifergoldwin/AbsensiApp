package com.example.absensiapp.service;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.absensiapp.R;
import com.example.absensiapp.model.Absensi;

import org.w3c.dom.Text;

import java.util.List;

public class ListAbsensiAdapter extends RecyclerView.Adapter<ListAbsensiAdapter.ViewHolder> {
    Context context;
    List<Absensi> absensiList;
    public ListAbsensiAdapter(Context context, List<Absensi> absensiList){
        this.context = context;
        this.absensiList = absensiList;
    }
    @NonNull
    @Override
    public ListAbsensiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_absensi,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAbsensiAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(absensiList.get(position).getFto1());
        holder.nama.setText("Nama            : "+absensiList.get(position).getNama());
        holder.lokasi.setText("Lokasi          : "+absensiList.get(position).getLokasi());
        holder.ket.setText(absensiList.get(position).getKet());
        holder.wkt.setText("Waktu absensi   : "+absensiList.get(position).getTglWkt());
        holder.nik.setText("NIK: "+absensiList.get(position).getNik());
        holder.np.setText("Nama Pendamping : "+absensiList.get(position).getNamaPendamping());
        holder.kab.setText("Kabupaten       : "+absensiList.get(position).getKabupaten());
    }

    @Override
    public int getItemCount() {
        return absensiList.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView nama;
        TextView ket;
        TextView lokasi;
        TextView wkt;
        TextView nik;
        TextView kab;
        TextView np;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.tv_nama_item);
            imageView = itemView.findViewById(R.id.iv_foto_item);
            wkt = itemView.findViewById(R.id.tv_wktu_item);
            ket = itemView.findViewById(R.id.tv_ket_item);
            lokasi = itemView.findViewById(R.id.tv_lokasi_item);
            nik = itemView.findViewById(R.id.tv_nik_item);
            np = itemView.findViewById(R.id.tv_np_item);
            kab = itemView.findViewById(R.id.tv_kab_item);
        }
    }
}
