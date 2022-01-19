package com.dmtfc.milia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Sing Up Activity
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class SingUpActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);
        progressDialog = new ProgressDialog(getApplicationContext());

        setTitle("MILIA - Реєстрація");

        final EditText emailText = (EditText) findViewById(R.id.editTextEmailAddressRegister);
        final EditText loginText = (EditText) findViewById(R.id.editTextLoginRegister);
        final EditText passwordText = (EditText) findViewById(R.id.editTextPasswordRegister);

        Button registerButton = (Button) findViewById(R.id.registerButton);

        //Check if edits are empty
        if (emailText.getText().toString() == "" || loginText.getText().toString() == "" || passwordText.getText().toString() == "") {
            Toast.makeText(getApplicationContext(), "Введіть в усі поля дані", Toast.LENGTH_SHORT).show();
        } else {
            //Process of registration
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUser.logOut(); // Log Out. because maybe user is logged
                    ParseUser user = new ParseUser(); //Create new user

                    //Add info about user
                    user.setEmail(emailText.getText().toString());
                    user.setUsername(loginText.getText().toString());
                    user.setPassword(passwordText.getText().toString());

                    progressDialog.show();
                    //Registration on server
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            progressDialog.dismiss();
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), "Ви успішно зареєструвались " + loginText.getText().toString(), Toast.LENGTH_SHORT).show();
                                Log.i("SingUp", "Success registration of " + loginText.getText().toString());
                                ParseUser.logOut();
                                startActivity(new Intent(SingUpActivity.this, MainActivity.class));
                                finish();
                                Toast.makeText(getApplicationContext(), "Авторизуйтесь, будь ласка", Toast.LENGTH_SHORT).show();
                            } else {
                                //Error of registration
                                Log.e("SingUp", "Error in Sing Up!");
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }
}