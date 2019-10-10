package com.digitalhouse.exerciciofirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalhouse.exerciciofirebase.adapter.PizzaAdapter;
import com.digitalhouse.exerciciofirebase.model.Pizza;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView bemVindoTextView;
    private ImageView usuarioImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private PizzaAdapter pizzaAdapter;
    private RecyclerView pizzaRecyclerView;
    private Button mussarelaButton;
    private Button calabresaButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bemVindoTextView = findViewById(R.id.bemvindo_text_view_id);
        usuarioImageView = findViewById(R.id.main_image_view_id);
        pizzaRecyclerView = findViewById(R.id.main_recycler_view_id);
        mussarelaButton = findViewById(R.id.button_mussarela_id);
        calabresaButton = findViewById(R.id.calabresa_button_id);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)  {
            String name = user.getDisplayName();
            bemVindoTextView.setText("Bem-vindo " +name);
        }

        usuarioImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tirarFoto();
            }
        });

        baixarFoto();

        pizzaAdapter = new PizzaAdapter();
        pizzaRecyclerView.setAdapter(pizzaAdapter);
        pizzaRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mussarelaButton.setOnClickListener(view -> {
            pedirPizza("mussarela", 5);
        });

        calabresaButton.setOnClickListener(view -> {
            pedirPizza("calabresa", 6);
        });


        atualizarRealTime();
    }

    private void atualizarRealTime() {
        db.collection("users")
                .document(user.getUid())
                .collection("pizzas")
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Pizza> pizzaList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : value) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Pizza pizza = new Pizza();
                            pizza.setDescricao((String) document.getData().get("descricao"));
                            pizza.setPreco((Double) document.getData().get("preco"));
                            pizza.setEntregue((Boolean) document.getData().get("entregue"));
                            pizzaList.add(pizza);
                        }
                        pizzaAdapter.atualizarPizzas(pizzaList);
                    }
                });
    }

    private void pedirPizza(String descricao, float preco) {
        Pizza pizza = new Pizza();
        pizza.setDescricao(descricao);
        pizza.setPreco(preco);
        pizza.setEntregue(false);
        pizza.setData(new Date());

        salvarFirebase(pizza);
    }

    private void salvarFirebase(Pizza pizza) {


        Map<String, Object> pizzaDb = new HashMap<>();
        pizzaDb.put("descricao", pizza.getDescricao());
        pizzaDb.put("preco", pizza.getPreco());
        pizzaDb.put("entregue", pizza.isEntregue());
        pizzaDb.put("data", new Timestamp(pizza.getData()));

// Add a new document with a generated ID
        db.collection("users")
                .document(user.getUid())
                .collection("pizzas")
                .add(pizzaDb)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void buscarDadosFirebase(){
        db.collection("users")
                .document(user.getUid())
                .collection("pizzas")
                .orderBy("data", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Pizza> pizzaList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Pizza pizza = new Pizza();
                                pizza.setDescricao((String) document.getData().get("descricao"));
                                pizza.setPreco((Double) document.getData().get("preco"));
                                pizza.setEntregue((Boolean) document.getData().get("entregue"));
                                pizzaList.add(pizza);
                            }
                            pizzaAdapter.atualizarPizzas(pizzaList);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void baixarFoto() {

        StorageReference reference = storage.getReference("perfil/"+ user.getUid());

        reference.getDownloadUrl().addOnSuccessListener(uri ->
                Picasso.get().load(uri).into(usuarioImageView)).addOnFailureListener(exception ->
                Toast.makeText(MainActivity.this, "Erro ao baixar a foto", Toast.LENGTH_SHORT).show());
    }


    private void tirarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            usuarioImageView.setImageBitmap(imageBitmap);

            StorageReference reference = storage.getReference("perfil/"+ user.getUid());

            // Get the data from an ImageView as bytes

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(MainActivity.this, "Upload fail", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Upload ok", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
