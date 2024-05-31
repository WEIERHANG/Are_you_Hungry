package com.hangandkai.areyouhungry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity_login extends AppCompatActivity {
    private String jsondata, jsondataToCode;
    private String usernames, passwords, loginCode1;
    private String baseUrl;
    private Context context;

    private void loadConfig() {
        Properties properties = new Properties();
        InputStream input = null;
        try {
            AssetManager assetManager = getAssets();
            input = assetManager.open("ipconfig.properties");
            properties.load(input);
            baseUrl = properties.getProperty("baseUrl1");
        } catch (IOException ex) {
            ex.printStackTrace();
            baseUrl = "http://default.url"; // 发生错误时使用的备用URL
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);
        final EditText username = findViewById(R.id.myUserName);
        final EditText password = findViewById(R.id.myPassword);
        password.setInputType(android.text.InputType.TYPE_CLASS_TEXT); // 设置为明文显示
        Button submit = findViewById(R.id.LoginBtn);
        Button code_Login = findViewById(R.id.RegisterBtn);
        loadConfig();

        submit.setOnClickListener(view -> {
            if (TextUtils.isEmpty(username.getText())) {
                Toast.makeText(MainActivity_login.this, "핸드폰 번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password.getText())) {
                Toast.makeText(MainActivity_login.this, "인증번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
            } else {
                usernames = username.getText().toString();
                passwords = password.getText().toString();
                if (loginCode1 != null) {
                    okhttpData();
                } else {
                    Toast.makeText(MainActivity_login.this, "인증번호를 먼저 받으십시오", Toast.LENGTH_SHORT).show();
                }
            }
        });

        code_Login.setOnClickListener(view -> {
            if (TextUtils.isEmpty(username.getText())) {
                Toast.makeText(MainActivity_login.this, "핸드폰 번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
            } else {
                usernames = username.getText().toString();
                requestVerificationCode(usernames);
            }
        });
    }

    private void okhttpData() {
        Log.i("로그인 코드:", loginCode1 != null ? loginCode1 : "loginCode1 is null");

        new Thread(() -> {
            if (usernames != null && loginCode1 != null) {
                OkHttpClient client = new OkHttpClient();
                FormBody formBody = new FormBody.Builder()
                        .add("phone", usernames)
                        .add("code", loginCode1)
                        .build();
                Request request = new Request.Builder()
                        .post(formBody)
                        .url(baseUrl + "/user/login/")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity_login.this, "네트워크 요청 실패", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        jsondata = response.body().string();
                        jsonJXData(jsondata);
                        response.body().close();
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity_login.this, "사용자 이름이나 인증 번호는 비워 둘 수 없습니다", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void jsonJXData(String jsondata) {
        if (jsondata != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsondata);
                JSONObject mapObject = jsonObject.getJSONObject("map");
                String data1 = mapObject.getString("sss");
                Log.i("인증 코드:", data1);
                runOnUiThread(() -> {
                    if ("1".equals(data1)) {
                        try {
                            JSONObject dataObject = jsonObject.getJSONObject("data"); // 获取data字段的JSONObject
                            String userId = dataObject.getString("id"); // 从data字段的JSONObject中获取id
                            String userPhone = dataObject.getString("phone"); // 从data字段的JSONObject中获取id

                            User user = new User(userId, userPhone); // 如果有其他用户信息也可以在这里传递
                            user.setId(userId);
                            user.setPhone(userPhone);

                            long userId1 = Long.parseLong(userId);

                            // 保存登陆的id
                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userId", userId);
                            editor.putLong("userIdLong", userId1);

                            editor.apply();

                            // 输出mapObject内容和userID
                            Log.d("MapObject", "MapObject Content: " + jsonObject.toString());
                            Log.d("UserID", "User ID: " + userId + "---" + userPhone);
                            Intent intent = new Intent(MainActivity_login.this, MainActivity_main.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity_login.this, "서버에 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity_login.this, "인증번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestVerificationCode(String phoneNumber) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("phone", usernames)
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(baseUrl + "/user/sendMsg/")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity_login.this, "인증 번호를 가져오지 못했습니다", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    jsondataToCode = response.body().string();
                    jsonJXDataToCode(jsondataToCode);
                    response.body().close();
                }
            });
        }).start();
    }

    private void jsonJXDataToCode(String jsondata) {
        loginCode1 = null;

        if (jsondata != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsondata);
                int code = jsonObject.getInt("code");
                if (code == 1) {
                    JSONObject mapObject = jsonObject.getJSONObject("map");
                    String loginCode = mapObject.getString("loginCode");
                    loginCode1 = loginCode;
                    runOnUiThread(() -> Toast.makeText(MainActivity_login.this, "인증 번호: " + loginCode, Toast.LENGTH_SHORT).show());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
