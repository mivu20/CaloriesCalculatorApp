package com.example.caloriecalculatorapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SignUp extends AppCompatActivity {
    private EditText edEmail,  edDOB, edHeight, edWeight;
    private Spinner spActivity;
    private Button btnSignup;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonGender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        initView();

        edDOB = findViewById(R.id.editTextDate);
        edDOB.setOnClickListener(view ->{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(SignUp.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    m++;
                    String date = d+"/"+m+"/"+y;
                    edDOB.setText(date);
                }
            },year, month, day);
            datePickerDialog.show();
        });

        //listener
        btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpSubmit();
            }
        });


    }

    //sign up submit
    public void signUpSubmit(){
        //error
        TextView tvErrorMess = (TextView) findViewById(R.id.tvErrorMessage);
        String errorMess = "";
        //email
        TextView tvEmail = (TextView) findViewById(R.id.tvEmail);
        edEmail = findViewById(R.id.editTextEmailAddress);
        String email = edEmail.getText().toString();
        if(email.isEmpty()){
            tvEmail.setTextColor(Color.RED);
            errorMess = "Please fill in an e-mail address";
        }
        else{
            tvEmail.setTextColor(Color.GRAY);
        }

        //DOB
        String dob = edDOB.getText().toString();

        //Gender
        radioGroupGender = (RadioGroup) findViewById(R.id.radioGroupGender);
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        radioButtonGender = findViewById(selectedId);
        String gender = radioButtonGender.getText().toString();

        //height
        String height = edHeight.getText().toString();
        int heightCm = 0;
        try {
            heightCm = Integer.parseInt(height);
        }
        catch(NumberFormatException e){
            errorMess = "Height has to be a number";
        }

        //weight
        String weight = edWeight.getText().toString();
        int weightKg = 0;
        try {
            weightKg = Integer.parseInt(weight);
        }
        catch(NumberFormatException e){
            errorMess = "Weight has to be a number";
        }

        //activity level
        String activity = spActivity.getSelectedItem().toString();
        int intactivity = spActivity.getSelectedItemPosition();

        //error handling
        if(errorMess.isEmpty()){
            //insert into database
            DBAdapter db = new DBAdapter(this);
            db.open();

            //Quote smart
            String stringEmailSQL = db.quoteSmart(email);
            String dateOfBirthsQL = db.quoteSmart(dob);
            String stringGenderSQL = db.quoteSmart(gender);
            int heightCmSQL = db.quoteSmart(heightCm);
            int intactivityLevelSQL = db.quoteSmart(intactivity);
            int weightKgSQL = db.quoteSmart(weightKg);

            //input for users
            String stringInput = "NULL, " + stringEmailSQL + "," + dateOfBirthsQL + "," + stringGenderSQL + "," + heightCmSQL+ "," + intactivityLevelSQL;
            db.insert("users", "_id, user_email, user_dob, user_gender, user_height, user_activity_level",
                    stringInput) ;

            //input for goals
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            String goalDate = df.format(Calendar.getInstance().getTime());

//            Calendar calendar = Calendar.getInstance();
//            int yearGoal = calendar.get(Calendar.YEAR);
//            int monthGoal = calendar.get(Calendar.MONTH);
//            int dayGoal = calendar.get(Calendar.DAY_OF_MONTH);
//            String goalDate = dayGoal + "/" + monthGoal + "/" + yearGoal;
            String goalDateSQL = db.quoteSmart(goalDate);
            stringInput = "NULL, " + weightKgSQL + "," + goalDateSQL ;
            db.insert("goals", "_id, goal_current_weight, goal_date",
                    stringInput) ;

            db.close();
            //move user to other Activity
            Intent i = new Intent(SignUp.this, SignUpGoal.class);
            startActivity(i);
        }
        else{
            tvErrorMess.setText(errorMess);
        }
    }

    private void initView(){
        edEmail = findViewById(R.id.editTextEmailAddress);
        edDOB = findViewById(R.id.editTextDate);
        edHeight = findViewById(R.id.editTextHeight);
        edWeight = findViewById(R.id.editTextWeight);
        spActivity = findViewById(R.id.spinnerActivity);
        btnSignup = findViewById(R.id.btnSignUp);
    }
}
