package com.example.proyectofinalciclo.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.proyectofinalciclo.Activities.MensajeGrupoMundialActivity;
import com.example.proyectofinalciclo.Activities.MensajeGrupoPrivadoActivity;
import com.example.proyectofinalciclo.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomSheetGrupoMundialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomSheetGrupoMundialFragment extends BottomSheetDialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BottomSheetGrupoMundialFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BottomSheetGrupoMundialFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BottomSheetGrupoMundialFragment newInstance(String param1, String param2) {
        BottomSheetGrupoMundialFragment fragment = new BottomSheetGrupoMundialFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_bottom_sheet_grupo_mundial, container, false);

        ImageView galeria=view.findViewById(R.id.galeria_fragment);
        ImageView pdf=view.findViewById(R.id.pdf_fragment);
        ImageView camara=view.findViewById(R.id.camara_fragment);
        ImageView word=view.findViewById(R.id.word_fragment);

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MensajeGrupoMundialActivity) getActivity()).Pdf();
                dismiss();
            }
        });

        word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MensajeGrupoMundialActivity) getActivity()).Word();
                dismiss();
            }
        });



        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MensajeGrupoMundialActivity) getActivity()).Imagen();
                dismiss();
            }
        });


        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MensajeGrupoMundialActivity) getActivity()).Camara();
                dismiss();
            }
        });
        return view;
    }
}