package com.example.inmueblecheck;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mDb;

    private final MutableLiveData<AuthResultState> _authResult = new MutableLiveData<>();
    public LiveData<AuthResultState> getAuthResult() { return _authResult; }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
    }

    public void login(String email, String password) {
        _authResult.setValue(AuthResultState.loading());
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchUserRole(mAuth.getCurrentUser());
                    } else {
                        _authResult.setValue(AuthResultState.error(Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
    }

    // AHORA RECIBE EL NOMBRE
    public void register(String nombre, String email, String password, String role) {
        _authResult.setValue(AuthResultState.loading());
        if (password.length() < 6) {
            _authResult.setValue(AuthResultState.error("La contraseña debe tener al menos 6 caracteres."));
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserRoleToFirestore(mAuth.getCurrentUser(), nombre, role);
                    } else {
                        _authResult.setValue(AuthResultState.error(Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
    }

    // AHORA GUARDA EL NOMBRE EN FIRESTORE
    private void saveUserRoleToFirestore(FirebaseUser user, String nombre, String role) {
        String uid = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("nombre", nombre); // Guardando el nombre
        userData.put("email", user.getEmail());
        userData.put("role", role);

        mDb.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    _authResult.setValue(AuthResultState.registrationSuccess());
                    mAuth.signOut();
                })
                .addOnFailureListener(e ->
                        _authResult.setValue(AuthResultState.error("Error al guardar datos: " + e.getMessage())));
    }

    private void fetchUserRole(FirebaseUser user) {
        if (user == null) return;
        mDb.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role == null) role = "arrendatario";
                        _authResult.setValue(AuthResultState.success(user, role));
                    } else {
                        _authResult.setValue(AuthResultState.error("Usuario sin rol asignado."));
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    _authResult.setValue(AuthResultState.error("Error de red: " + e.getMessage()));
                    mAuth.signOut();
                });
    }

    public static class AuthResultState {
        public enum Status { SUCCESS, ERROR, LOADING, REGISTRATION_SUCCESS }
        public final Status status;
        public final FirebaseUser user;
        public final String role;
        public final String errorMessage;

        private AuthResultState(Status status, FirebaseUser user, String role, String errorMessage) {
            this.status = status;
            this.user = user;
            this.role = role;
            this.errorMessage = errorMessage;
        }
        public static AuthResultState loading() { return new AuthResultState(Status.LOADING, null, null, null); }
        public static AuthResultState success(FirebaseUser u, String r) { return new AuthResultState(Status.SUCCESS, u, r, null); }
        public static AuthResultState registrationSuccess() { return new AuthResultState(Status.REGISTRATION_SUCCESS, null, null, null); }
        public static AuthResultState error(String msg) { return new AuthResultState(Status.ERROR, null, null, msg); }
    }
}