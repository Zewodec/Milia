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

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Login Activity =)
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(getApplicationContext());

        setTitle("MILIA - Авторизація");

        Button AuthButton = (Button) findViewById(R.id.authButton);

        final EditText editTextLogin = (EditText) findViewById(R.id.editTextLoginRegister);
        final EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);

//        ParseUser.logOut();

        //Check if logged
        if (ParseUser.getCurrentUser() != null) {
            AlreadyUserLoggedGoNext();
//            if (ParseUser.getCurrentUser().isAuthenticated()) {}
        }

        if (editTextLogin.getText().toString() == "" || editTextPassword.getText().toString() == "") {
            Toast.makeText(getApplicationContext(), "Поле логін або пароль - пусте.", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Введіть логін і пароль, будь ласка", Toast.LENGTH_SHORT).show();
        } else {
            //Auth button
            AuthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.show();
                    //Auth in parse system
                    ParseUser.logInInBackground(
                            editTextLogin.getText().toString(),
                            editTextPassword.getText().toString(),
                            new LogInCallback() {
                                @Override
                                public void done(ParseUser parseUser, ParseException e) {
                                    progressDialog.dismiss();
                                    if (parseUser != null) {
                                        Toast.makeText(getApplicationContext(), "Авторизація успішна!", Toast.LENGTH_SHORT).show();
                                        Log.i("Auth", "Sign In: OK!");

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

        // ParseUser.logOut();

    /*
    if (ParseUser.getCurrentUser() != null){
      Log.i("Auth", "Sing In: " + ParseUser.getCurrentUser().getUsername());
    } else{
      Log.i("Auth", "Sing not In");
    }

    ParseUser.logInInBackground("Oleg", "Kuzo", new LogInCallback() {
      @Override
      public void done(ParseUser parseUser, ParseException e) {
        if (parseUser != null){
          Log.i("SingIn", "Success SingIn");
        } else{
          e.printStackTrace();
        }
      }
    });


    ParseUser user = new ParseUser();

    user.setUsername("Oleg");
    user.setPassword("Kuzo");

    user.signUpInBackground(new SignUpCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null){
          Log.i("Registration", "Sing UP = NICE");
        }
      }
    });


        //Create a tweet class, username, tweet, save it to parse, query it, update the tweet

    ParseObject tweetClass = new ParseObject("Tweets");

    tweetClass.put("username", "Nazar");
    tweetClass.put("tweet", "Hello World!");

    tweetClass.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null){
          Log.i("Save", "Save succesufull!");
        }else {
          e.printStackTrace();
        }
      }
    });

    ParseQuery<ParseObject> query = ParseQuery.getQuery("Tweets");

    query.getInBackground("EJMjEx1EMO", new GetCallback<ParseObject>() {
      @Override
      public void done(ParseObject parseObject, ParseException e) {
        if (e == null && parseObject != null){
          parseObject.put("tweet", "It is not Hello World!");
          parseObject.saveInBackground();

          Log.i("Username", parseObject.getString("username"));
          Log.i("Tweet", parseObject.getString("tweet"));
        }
      }
    });

    ParseQuery<ParseObject> query = ParseQuery.getQuery("Tweets");

    query.whereContains("tweet", "Hello");
    query.setLimit(1);

    query.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> list, ParseException e) {
        if (e == null){
          if (list.size() > 0){
            for (ParseObject object : list){
              object.put("tweet", "It is Hello World!");
              object.saveInBackground();
              Log.i("Username", object.getString("username"));
              Log.i("Tweet", object.getString("tweet"));
            }
          }
        }
      }
    });
    */

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    //Change current activity to User List activity
    private void SwitchActivityToUserList() {
        Intent intent = new Intent(MainActivity.this, UserListActivity.class);
        startActivity(intent);
        finish();
    }

    //If User is already logged than go to UserList Activity
    private void AlreadyUserLoggedGoNext() {
        Intent intent = new Intent(MainActivity.this, UserListActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // delete all other activity and
        startActivity(intent);
        finish();
    }

    //Change to Sign Up Activity
    //TO DO: Nice transition between them
    private void ChangeSceneToSignUp() {
        /*
        Scene registerScene;

        ViewGroup sceneRoot = (ViewGroup) findViewById(R.id.mainActivityScene);

        registerScene = Scene.getSceneForLayout(sceneRoot, R.layout.activity_sing_up, this);

//        Transition changeTranstion = TransitionInflater.from(this).inflateTransition(R.transition.change_scene);
        Transition ct = new AutoTransition();
        TransitionManager.go(registerScene, ct);*/
        Intent intent = new Intent(MainActivity.this, SingUpActivity.class);
        startActivity(intent);
    }
}