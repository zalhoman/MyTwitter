package zalho.com.br.mytwitter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter arrayAdapter;

    private ArrayList<String> usuarios;
    private ArrayList<String> userIds;
    private ArrayList<String> seguindo;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference();

    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    private String meuUid;
    private String meuEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        seguindo = new ArrayList<>();
        usuarios = new ArrayList<>();
        userIds = new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, usuarios);
        listView = (ListView) findViewById(R.id.lv_dashboard);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if(checkedTextView.isChecked())
                    seguindo.add(userIds.get(position));
                else
                    seguindo.remove(seguindo.indexOf(userIds.get(position)));

                ref.child("users").child(meuUid).child("seguindo").setValue(seguindo);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            finish();
        } else {
            meuUid = user.getUid();
            ref.child("users").child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    meuEmail = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            userIds.clear();
            usuarios.clear();

            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(!dataSnapshot.child("uid").getValue(String.class).equals(meuUid)){
                        usuarios.add(dataSnapshot.child("email").getValue(String.class));
                        userIds.add(dataSnapshot.child("uid").getValue(String.class));
                        arrayAdapter.notifyDataSetChanged();
                        atualizarLista();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            ref.child("users").addChildEventListener(childEventListener);

            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    seguindo.clear();
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        seguindo.add(data.getValue(String.class));
                    }
                    Log.d("Seguindo", "Seguindo: " + seguindo);
                    atualizarLista();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            ref.child("users").child(meuUid).child("seguindo").addValueEventListener(valueEventListener);
        }
    }

    public void atualizarLista(){
        for(String uid : userIds){
            if(seguindo.contains(uid)){
                listView.setItemChecked(userIds.indexOf(uid), true);
            }else {
                listView.setItemChecked(userIds.indexOf(uid), false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId){
            case R.id.menu_feed:
                break;
            case R.id.menu_tweet:
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
