package zalho.com.br.mytwitter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText campoEmail;
    private EditText campoSenha;
    private Button btnEntrar;
    private Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = (EditText) findViewById(R.id.et_email);
        campoSenha = (EditText) findViewById(R.id.et_senha);
        btnEntrar = (Button) findViewById(R.id.btn_entrar);
        btnCadastrar = (Button) findViewById(R.id.btn_cadastrar);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    Log.d("Conexao passou", "Usuário conectado" + user.getEmail());
                    Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(i);
                    campoEmail.setText("");
                    campoSenha.setText("");
                } else {
                    Log.e("Falha Conexão", "Não há usuários conectados");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void realizaLogin(View view){
        mAuth.signInWithEmailAndPassword(campoEmail.getText().toString(), campoSenha.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.d("Falha Login", "Falha na autenticação");
                }
            }
        });
    }

    public void criaUsuario(View view){
        mAuth.createUserWithEmailAndPassword(campoEmail.getText().toString(), campoSenha.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Log.e("Erro Cadastrar", "Erro ao cadastrar usuario");
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference();
                    reference.child("email").setValue(campoEmail.getText().toString());
                    reference.child("uid").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
            }
        });
    }
}
