package choongyul.android.com.firebasecloudmessage;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText etMessage,etID,etPW;
    TextView tvToken;
    Button btnSend,btnToken,btnSignin;
    FirebaseDatabase database;
    DatabaseReference userRef;
    ListView listview;
    List<User> datas = new ArrayList<>();
    ListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위젯
        etMessage = (EditText) findViewById(R.id.etMessage);
        etID = (EditText) findViewById(R.id.etID);
        etPW = (EditText) findViewById(R.id.etPW);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSignin = (Button) findViewById(R.id.btnSignin);
        tvToken = (TextView) findViewById(R.id.tvToken);

        // 리스트
        listview = (ListView) findViewById(R.id.listView);
        adapter = new ListAdapter(this, datas);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = datas.get(position);
                tvToken.setText(user.getToken());
            }
        });

        // 파이어베이스
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("user");
    }
    String result = "";

    public void sendNotification (View view) {

        final String sender = etID.getText().toString();
        final String msg = etMessage.getText().toString();
        final String token = tvToken.getText().toString();

        if("".equals(msg)) { // 입력값이 있을때만 날려주기 위함.
            Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if("".equals(token)){ //
            Toast.makeText(this, "상대방을 정해주세요", Toast.LENGTH_SHORT).show();
        } else {
            new AsyncTask<Void,Void,String >() {
                @Override
                protected String doInBackground(Void... params) {
                    result = "메시지를 전송하였습니다.";

                    // 1. fcm 서버정보 세팅
                    String server_url = "http://192.168.1.190:8080/fcm/sender";
                    // 2. POST message 세팅
                    String post_data = "receiver_token="+token+"&msg="+msg+"&sender="+sender;

                    // 3. HttpUrlConnection 을 사용해서 FCM 서버측으로 메시지를 전송한다.
                    // 		a. 서버연결
                    try {
                        URL url = new URL(server_url);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        //		b.header 설정
                        con.setRequestMethod("POST");
                        //		c.POST데이터(body) 전송
                        con.setDoOutput(true);
                        OutputStream os = con.getOutputStream();
                        os.write(post_data.getBytes());
                        os.flush();
                        os.close();
                        //		d.전송후 결과처리
                        int responseCode = con.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) { // code 200 이 HTTP_OK 의 코드이다.
                            // 결과 처리 후 FCM 서버측에서 발송한 결과 메시지를 꺼낸다.
                            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            // 4.2 반복문은 돌면서 버퍼의 데이터를 읽어온다.
                            String dataLine = "";
                            // 메시지를 한줄씩 읽어서 result변수에 담아두고
                            while ((dataLine = br.readLine()) != null) {
                                result = result + dataLine;
                            }
                            br.close();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            }.execute();


        }

    }
    public void signIn(View view){
        final String id = etID.getText().toString();
        final String pw = etPW.getText().toString();

        // DB 1. 파이어베이스로 child(id) 레퍼런스에 대한 쿼리를 날린다.
        userRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            // DB 2. 파이어베이스는 데이터쿼리가 완료되면 스냅샷에 담아서 onDataChange를 호출해준다.
            // 현재 데이터베이스로 본다면 datasnapshot는 aaa - name - 홍길동 , aaa- password - 123을 넘겨준다
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    String fbPw = dataSnapshot.child("password").getValue().toString();
//                    String name = dataSnapshot.child("name").getValue().toString();
                    if (fbPw.equals(pw)) {
                        addToken();
                        setList();

                    } else {
                        Toast.makeText(MainActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User 가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addToken(){
        final String id = etID.getText().toString();
        userRef.child(id).child("token").setValue(getToken());
    }

    public void setList() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                datas.clear();
                for(DataSnapshot data: dataSnapshot.getChildren() ){
                    User user = data.getValue(User.class);
                    user.setId(data.getKey());
                    datas.add(user);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e("Token ==== ",token);
        return token;

    }
}


class ListAdapter extends BaseAdapter {

    Context context;
    List<User> datas;
    LayoutInflater inflater;

    public ListAdapter(Context context, List<User> datas) {
        this.context = context;
        this.datas = datas;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.item_list, null);
        }
        User user = datas.get(position);
        TextView userid = (TextView) convertView.findViewById(R.id.userid);
        userid.setText(user.getId());
        return convertView;
    }
}