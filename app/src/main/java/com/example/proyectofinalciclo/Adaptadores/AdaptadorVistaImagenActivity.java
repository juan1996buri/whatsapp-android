package com.example.proyectofinalciclo.Adaptadores;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.proyectofinalciclo.Activities.ConfiguracionPerfilPersonalActivity;
import com.example.proyectofinalciclo.Clases.Mensaje;
import com.example.proyectofinalciclo.R;
import com.example.proyectofinalciclo.databinding.SliderItemLayoutBinding;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdaptadorVistaImagenActivity extends SliderViewAdapter<AdaptadorVistaImagenActivity.SliderAdapterVH> {

    private Context context;
    private ArrayList<Mensaje> mensajes;


    public AdaptadorVistaImagenActivity(Context context, ArrayList<Mensaje> mensajes) {
        this.context = context;
        this.mensajes = mensajes;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item_layout, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        Mensaje mensaje= mensajes.get(position);

        Picasso.with(context).load(mensaje.getMensajeTxt())
                .placeholder(R.drawable.plceholder)
                .error(R.drawable.plceholder)
                .into(viewHolder.imageView);

    }

    @Override
    public int getCount() {

        return mensajes.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        ImageView imageView;
        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.image_view);
        }
    }

}