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

import com.onesignal.OneSignal;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Login and start Activity =)
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("MILIA - Авторизація");

        Button AuthButton = (Button) findViewById(R.id.authButton);

        final EditText editTextLogin = (EditText) findViewById(R.id.editTextLoginRegister);
        final EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        // Check if logged
        if (ParseUser.getCurrentUser() != null) {
            // check if authenticated
            if (ParseUser.getCurrentUser().isAuthenticated()) {
                // If yes go on List of users layout (UserListActivity)
                AlreadyUserLoggedGoNext();
                // Set external unique if for push messages when logged
                String userID = ParseUser.getCurrentUser().getObjectId();
                OneSignal.setExternalUserId(userID);
            }
        }

        // check if login or password field is empty
        if (editTextLogin.getText().toString() == "" || editTextPassword.getText().toString() == "") {
            Toast.makeText(getApplicationContext(), "Поле логін або пароль - пусте.", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Введіть логін і пароль, будь ласка", Toast.LENGTH_SHORT).show();
        } else {
            //Auth button
            AuthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Auth in parse system
                    ParseUser.logInInBackground(
                            editTextLogin.getText().toString(),
                            editTextPassword.getText().toString(),
                            new LogInCallback() {
                                @Override
                                public void done(ParseUser parseUser, ParseException e) {
                                    if (parseUser != null) {
                                        Toast.makeText(getApplicationContext(), "Авторизація успішна!", Toast.LENGTH_SHORT).show();
                                        Log.i("Auth", "Sign In: OK!");
                                        // Set external unique if for push messages when logging
                                        String userID = ParseUser.getCurrentUser().getObjectId();
                                        OneSignal.setExternalUserId(userID);

                                        // Go to User List Activity
                                        SwitchActivityToUserList();
                                    } else {
                                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            });
        }
        //Go to registration activity
        Button moveToRegistration = (Button) findViewById(R.id.buttonToRegister);
        moveToRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeSceneToSignUp();
            }
        });
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    //Change current activity to User List activity
    private void SwitchActivityToUserList() {
        Intent intent = new Intent(MainActivity.this, UserListActivity.class);
        startActivity(intent);
        finish();
    }

    // If User is already logged than go to UserList Activity
    private void AlreadyUserLoggedGoNext() {
        Intent intent = new Intent(MainActivity.this, UserListActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // delete all other activity and
        startActivity(intent);
        finish();
    }

    // Change to Sign Up Activity
    // TODO: Nice transition between them in future
    private void ChangeSceneToSignUp() {
        Intent intent = new Intent(MainActivity.this, SingUpActivity.class);
        startActivity(intent);
    }
}