package com.digitalhouse.exerciciofirebase.repository;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.digitalhouse.exerciciofirebase.modules.login.view.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.Completable;

public class FirebaseRepository {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final String TAG = "LoginActivity";

    public Completable autenticar(String email, String senha){
        return Completable.create(emitter ->{
            firebaseAuth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                emitter.onComplete();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                emitter.onError(task.getException());
                            }

                        }
                    });
                });
    }

}
