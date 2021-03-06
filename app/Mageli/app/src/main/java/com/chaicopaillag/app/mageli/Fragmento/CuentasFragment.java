package com.chaicopaillag.app.mageli.Fragmento;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chaicopaillag.app.mageli.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class CuentasFragment extends Fragment {
    private CircleImageView img_cuenta;
    private TextView nombre,correo,proveedor,fecha,uuid;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    public CuentasFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_cuentas, container, false);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        img_cuenta=(CircleImageView)view.findViewById(R.id.img_perfil_cuenta);
        nombre=(TextView)view.findViewById(R.id.nombre_cuenta);
        correo=(TextView)view.findViewById(R.id.correo_cuenta);
        proveedor=(TextView)view.findViewById(R.id.provedor_cuenta);
        fecha=(TextView)view.findViewById(R.id.fecha_creado_cuenta);
        uuid=(TextView)view.findViewById(R.id.uid_cuenta);

        nombre.setText(firebaseUser.getDisplayName());
        correo.setText(firebaseUser.getEmail());
        proveedor.setText(firebaseUser.getProviderId());
        if(firebaseUser.isEmailVerified()){
            fecha.setText("Cuenta Verificada");
        }else {
            fecha.setText("Cuenta No Verificada");
        }
        uuid.setText(firebaseUser.getUid());
        if (firebaseUser.getPhotoUrl()!=null){
            Glide.with(getContext()).load(firebaseUser.getPhotoUrl()).into(img_cuenta);
        }
        return view;
    }
}
