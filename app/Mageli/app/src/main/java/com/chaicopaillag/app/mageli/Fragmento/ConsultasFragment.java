package com.chaicopaillag.app.mageli.Fragmento;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chaicopaillag.app.mageli.Activity.ConsultaActivity;
import com.chaicopaillag.app.mageli.Adapter.ConsultasAdapter;
import com.chaicopaillag.app.mageli.Modelo.Consulta;
import com.chaicopaillag.app.mageli.Modelo.RespuestaConsulta;
import com.chaicopaillag.app.mageli.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;

public class ConsultasFragment extends Fragment {
    private RecyclerView recyclerViewconsulta;
    private DatabaseReference firebase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseRecyclerOptions item_consulta;
    private FloatingActionButton fab_agregar_conculta;
    private LinearLayout sinconsulta;
    private FirebaseRecyclerAdapter<Consulta,ConsultasAdapter.ViewHolder>adapter;
    public ConsultasFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_consultas, container, false);;
        inicializar_controles(view);
        inicializar_servicio();
        return view;
    }

    private void inicializar_servicio() {
        firebase= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewconsulta.setLayoutManager(linearLayoutManager);
        Query query=firebase.child("Consultas").orderByChild("uid_paciente").equalTo(firebaseUser.getUid()).limitToFirst(100);
         item_consulta=new FirebaseRecyclerOptions.Builder<Consulta>().setQuery(query,Consulta.class).build();
         adapter= new FirebaseRecyclerAdapter<Consulta, ConsultasAdapter.ViewHolder>(item_consulta) {
            @Override
            protected void onBindViewHolder(@NonNull ConsultasAdapter.ViewHolder holder, final int position, @NonNull final Consulta model) {
                holder.setAsunto(model.getAsunto());
                holder.setDescripcion(model.getDescripcion());
                holder.setNombre_pediatra(model.getNombre_pediatra()+ " - "+getString(R.string.pediatra));
                holder.setFecha_consulta(model.getFecha_registro());
                Glide.with(getActivity().getApplicationContext()).load(model.getUrl_img_padiatra()).into(holder.img_pediatra);
                if (model.isFlag_respuesta()){
                    holder.setRespuesta(getString(R.string.si_respuesta));
                }else {
                    holder.setRespuesta(getString(R.string.no_respuesta));
                }
                holder.btn_respuesta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(model.isFlag_respuesta()){
                            cargar_respuestas(model);
                        }
                    }
                });
                holder.respuesta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(model.isFlag_respuesta()){
                            cargar_respuestas(model);
                        }
                    }
                });
                holder.btn_editar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.isFlag_respuesta()){
                            Toast.makeText(getContext(),getString(R.string.no_editar_consulta), Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent= new Intent(getContext(),ConsultaActivity.class);
                            intent.putExtra("editar_consulta",true);
                            intent.putExtra("uid_consulta",model.getId());
                            startActivity(intent);
                        }
                    }
                });
                holder.btn_eliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder alert_consulta = new AlertDialog.Builder(getContext(),R.style.progrescolor);
                        alert_consulta.setTitle(R.string.app_name);
                        alert_consulta.setMessage(R.string.eliminar_consulta);
                        alert_consulta.setPositiveButton(R.string.si,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    adapter.getRef(position).removeValue();
                                }catch (Exception e){
                                    Log.e("Exception en", e.getMessage());
                                }
                            }
                        });
                        alert_consulta.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alert_consulta.show();
                    }
                });
            }
            @NonNull
            @Override
            public ConsultasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.consultas_item, parent, false);
                return new ConsultasAdapter.ViewHolder(view);
            }

             @Override
             public void onDataChanged() {
                 super.onDataChanged();
                 if (adapter.getItemCount()>0){
                     recyclerViewconsulta.setVisibility(View.VISIBLE);
                     sinconsulta.setVisibility(View.INVISIBLE);
                 }else {
                     recyclerViewconsulta.setVisibility(View.GONE);
                     sinconsulta.setVisibility(View.VISIBLE);
                 }
             }
         };
         recyclerViewconsulta.setAdapter(adapter);
        recyclerViewconsulta.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState== recyclerViewconsulta.SCROLL_STATE_IDLE){
                    fab_agregar_conculta.show();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy>0 || dy<0 && fab_agregar_conculta.isShown()){
                    fab_agregar_conculta.hide();
                }
            }
        });
    }

    private void cargar_respuestas(final Consulta model) {
    firebase.child("RespuestasConsulta").child(model.getId()).orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            RespuestaConsulta respuestaConsulta= dataSnapshot.getValue(RespuestaConsulta.class);
            if (respuestaConsulta!=null){
                LayoutInflater inflater=getLayoutInflater();
                View popap= inflater.inflate(R.layout.respuestas_consulta,null);
                TextView asunto_consulta_respuesta=popap.findViewById(R.id.asunto_consulta_respuesta);
                TextView respuesta_pediatra=popap.findViewById(R.id.respondido_pediatra);
                TextView descripcion= popap.findViewById(R.id.respuesta_descripcion);
                asunto_consulta_respuesta.setText(model.getAsunto());
                respuesta_pediatra.setText(model.getNombre_pediatra());
                descripcion.setText(respuestaConsulta.getDescripcion());
                final AlertDialog.Builder popap_respuesta= new AlertDialog.Builder(Objects.requireNonNull(getContext()), R.style.progrescolor);
                popap_respuesta.setView(popap);
                popap_respuesta.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                popap_respuesta.show();
            }else {
                Toast.makeText(getContext(), R.string.consulta_sin_respuesta, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private void inicializar_controles(View view) {
        fab_agregar_conculta=(FloatingActionButton) view.findViewById(R.id.fab_agregar_consultas);
        recyclerViewconsulta=(RecyclerView)view.findViewById(R.id.recy_consultas);
        sinconsulta=(LinearLayout)view.findViewById(R.id.layautSinConsulta);
        fab_agregar_conculta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ConsultaActivity.class);
                startActivity(intent);
            }
        });
    }

}
