package com.example.chatapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int RC_GET_IMAGE = 128;

    private RecyclerView recyclerViewMessages;
    private MessageAdapter adapterMessages;

    private EditText editTextMessage;
    private ImageView imageViewSend;
    private ImageView imageViewAddImage;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        imageViewSend = findViewById(R.id.imageViewSendMessage);
        imageViewAddImage = findViewById(R.id.imageViewAddImage);
        adapterMessages = new MessageAdapter();
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(adapterMessages);
        if (mAuth.getCurrentUser() != null) {
            imageViewSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String textOfMessage = editTextMessage.getText().toString().trim();
                    sendMessage(textOfMessage, null);
                }
            });
            imageViewAddImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(intent, RC_GET_IMAGE);
                }
            });
        } else {
            singOut();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = mAuth.getCurrentUser();
        db.collection("messages").orderBy("createdTime").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<Message> messages = null;
                if (value != null) {
                    messages = value.toObjects(Message.class);
                    adapterMessages.setMessages(messages);
                    recyclerViewMessages.scrollToPosition(adapterMessages.getItemCount() - 1);
                }
            }
        });
        recyclerViewMessages.scrollToPosition(adapterMessages.getItemCount() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_exit) {
            singOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String textOfMessage, String imageUrl) {
        long createdTime = System.currentTimeMillis();
        Message message = null;
        if (textOfMessage != null &&!textOfMessage.isEmpty()) {
            message = new Message(user.getEmail(), textOfMessage, createdTime, null);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            message = new Message(user.getEmail(), null, createdTime, imageUrl);
        } else {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
        }
        editTextMessage.setText("");
        db.collection("messages")
                .add(message)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        editTextMessage.setText(textOfMessage);
                        Toast.makeText(MainActivity.this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GET_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri image = data.getData();
                StorageReference referenceToImages = reference.child("images/" + image.getLastPathSegment());
                referenceToImages.putFile(image)
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return referenceToImages.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                sendMessage(null, downloadUri.toString());
                            }
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });
            }
        }
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            user = mAuth.getCurrentUser();
            Toast.makeText(this, "Вы вошли как " + user.getEmail(), Toast.LENGTH_SHORT).show();
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }


    public void singOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            List<AuthUI.IdpConfig> providers = Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build());
                            Intent signInIntent = AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build();
                            signInLauncher.launch(signInIntent);
                        }
                    }
                });


    }
}