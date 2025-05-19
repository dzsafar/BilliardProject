package com.example.billiardproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class beallitasok extends AppCompatActivity {
    private static final String LOG_TAG = beallitasok.class.getName();
    private static final String PREF_KEY = register_activity.class.getPackage().toString();

    TextView textv;
    private SharedPreferences preferences;
    private FirebaseUser user;

    private EditText editTextAsztal;
    private EditText editTextIdopont;
    private Button buttonLetrehoz;
    private Button buttonOlvas;
    private Button buttonFrissit;
    private Button buttonTorol;
    private Button buttonVissza;
    private TextView textViewEredmeny;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference beallitasokRef = db.collection("Beallitasok");

    private String dokumentumId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beallitasok);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        editTextAsztal = findViewById(R.id.editTextAsztal);
        editTextIdopont = findViewById(R.id.editTextIdopont);
        buttonLetrehoz = findViewById(R.id.buttonLetrehoz);
        buttonOlvas = findViewById(R.id.buttonOlvas);
        buttonFrissit = findViewById(R.id.buttonFrissit);
        buttonTorol = findViewById(R.id.buttonTorol);
        buttonVissza = findViewById(R.id.buttonVissza);
        textViewEredmeny = findViewById(R.id.textViewEredmeny);

        // Létrehozás (Create)
        buttonLetrehoz.setOnClickListener(v -> {
            String asztal = editTextAsztal.getText().toString();
            String idopont = editTextIdopont.getText().toString();
            if (!asztal.isEmpty() && !idopont.isEmpty()) {
                BeallitasAdat adat = new BeallitasAdat(asztal, idopont);
                beallitasokRef.add(adat)
                        .addOnSuccessListener(documentReference -> {
                            dokumentumId = documentReference.getId();
                            String uzenet = "Foglalás létrehozva.  Asztal: " + asztal + ", Időpont: " + idopont;
                            Toast.makeText(this, uzenet, Toast.LENGTH_SHORT).show();
                            Log.d(LOG_TAG, uzenet);
                            editTextAsztal.setText("");
                            editTextIdopont.setText("");
                            textViewEredmeny.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Log.w(LOG_TAG, "Hiba az adat létrehozásakor", e);
                            Toast.makeText(this, "Hiba az adat létrehozásakor", Toast.LENGTH_SHORT).show();
                            textViewEredmeny.setText("");
                        });
            } else {
                Toast.makeText(this, "Az asztal és az időpont nem lehet üres", Toast.LENGTH_SHORT).show();
                textViewEredmeny.setText("");
            }
        });

        // Olvasás (Read)
        buttonOlvas.setOnClickListener(v -> {
            if (!dokumentumId.isEmpty()) {
                DocumentReference docRef = beallitasokRef.document(dokumentumId);
                docRef.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                BeallitasAdat adat = documentSnapshot.toObject(BeallitasAdat.class);
                                if (adat != null) {
                                    String eredmeny = "Asztal: " + adat.getAsztal() + "\nIdőpont: " + adat.getIdopont();
                                    textViewEredmeny.setText(eredmeny);
                                    Log.d(LOG_TAG, "Adat lekérdezve: " + eredmeny);
                                } else {
                                    textViewEredmeny.setText("Hiba: Az adat konvertálása sikertelen");
                                    Log.e(LOG_TAG, "Hiba az adat konvertálásakor");
                                }
                            } else {
                                textViewEredmeny.setText("Nincs foglalás ehhez az azonosítóhoz.");
                                Log.d(LOG_TAG, "Nincs ilyen ID-val dokumentum");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(LOG_TAG, "Hiba az adat olvasásakor", e);
                            Toast.makeText(this, "Hiba az adat olvasásakor", Toast.LENGTH_SHORT).show();
                            textViewEredmeny.setText("Hiba: " + e.getMessage());
                        });
            } else {
                Toast.makeText(this, "Előbb hozz létre egy foglalást az olvasáshoz.", Toast.LENGTH_SHORT).show();
                textViewEredmeny.setText("");
            }
        });

        // Frissítés (Update)
        buttonFrissit.setOnClickListener(v -> {
            if (!dokumentumId.isEmpty()) {
                String ujAsztal = editTextAsztal.getText().toString();
                String ujIdopont = editTextIdopont.getText().toString();
                if (!ujAsztal.isEmpty() && !ujIdopont.isEmpty()) {
                    DocumentReference docRef = beallitasokRef.document(dokumentumId);
                    docRef.update("asztal", ujAsztal, "idopont", ujIdopont)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Foglalás sikeresen frissítve", Toast.LENGTH_SHORT).show();
                                editTextAsztal.setText("");
                                editTextIdopont.setText("");
                                textViewEredmeny.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Log.w(LOG_TAG, "Hiba a foglalás frissítésekor", e);
                                Toast.makeText(this, "Hiba a foglalás frissítésekor", Toast.LENGTH_SHORT).show();
                                textViewEredmeny.setText("");
                            });
                } else {
                    Toast.makeText(this, "Az asztal és az időpont nem lehet üres a frissítéshez", Toast.LENGTH_SHORT).show();
                    textViewEredmeny.setText("");
                }
            } else {
                Toast.makeText(this, "Előbb hozz létre egy foglalást a frissítéshez", Toast.LENGTH_SHORT).show();
                textViewEredmeny.setText("");
            }
        });

        // Törlés (Delete)
        buttonTorol.setOnClickListener(v -> {
            if (!dokumentumId.isEmpty()) {
                DocumentReference docRef = beallitasokRef.document(dokumentumId);
                docRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Foglalás sikeresen törölve", Toast.LENGTH_SHORT).show();
                            dokumentumId = "";
                            editTextAsztal.setText("");
                            editTextIdopont.setText("");
                            textViewEredmeny.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Log.w(LOG_TAG, "Hiba a foglalás törlésekor", e);
                            Toast.makeText(this, "Hiba a foglalás törlésekor", Toast.LENGTH_SHORT).show();
                            textViewEredmeny.setText("");
                        });
            } else {
                Toast.makeText(this, "Előbb hozz létre egy foglalást a törléshez", Toast.LENGTH_SHORT).show();
                textViewEredmeny.setText("");
            }
        });


        buttonVissza.setOnClickListener(v -> {
            finish();
        });
    }
}
