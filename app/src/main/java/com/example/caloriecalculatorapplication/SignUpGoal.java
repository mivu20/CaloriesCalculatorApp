package com.example.caloriecalculatorapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SignUpGoal extends AppCompatActivity {
    private EditText edTarget, edWeekly;
    private Spinner spWeekly;
    private Button btnLetsStart;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_goal);
        initView();

        //listener

        btnLetsStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                letsStartSubmit();
            }
        });
    }

    public void letsStartSubmit(){
        //insert into database
        DBAdapter db = new DBAdapter(this);
        db.open();

        //error
        TextView tvErrorMessGoal = (TextView) findViewById(R.id.tvErrorMessageGoal);
        String errorMessGoal = "";

        //target
        String target = edTarget.getText().toString();
        double targetKg = 0;
        try {
            targetKg = Double.parseDouble(target);
        }
        catch(NumberFormatException e){
            errorMessGoal = "Weight has to be a number";
        }


        //weekly weight
        String weekly = edWeekly.getText().toString();
        double weeklyKg = 0;
        try {
            weeklyKg = Double.parseDouble(weekly);
        }
        catch(NumberFormatException e){
            errorMessGoal = "Weight has to be a number";
        }

        //spinner i want to
        String spIWantTo = spWeekly.getSelectedItem().toString();
        int intIWantTo = spWeekly.getSelectedItemPosition();

        //update fields
        if(errorMessGoal.isEmpty()){
            //Quote smart
            double targetKgSQL = db.quoteSmart(targetKg);
            String spIWantToSQL = db.quoteSmart(spIWantTo);
            int intIWantToSQL = db.quoteSmart(intIWantTo);
            double weeklyKgSQL = db.quoteSmart(weeklyKg);

            long goalID =1;
            //String stringInput = targetKgSQL + "," + spIWantToSQL + "," + weeklyKgSQL;
            db.update("goals", "_id", goalID, "goal_target_weight", targetKgSQL) ;
            db.update("goals", "_id", goalID, "goal_notes", spIWantToSQL) ;
            db.update("goals", "_id", goalID, "goal_iwantto", intIWantToSQL) ;
            db.update("goals", "_id", goalID, "goal_weekly_goal", weeklyKgSQL) ;
        }

        //calculate energy
        if(errorMessGoal.isEmpty()){
            // get row number one from user
            long rowID =1;
            String fields[] = new String[]{"_id", "user_dob", "user_gender", "user_height", "user_height", "user_activity_level"};
            Cursor c =db.selectPrimaryKey("users", "_id", rowID, fields);
            String stringUserDOB = c.getString(1);
            String stringUserGender = c.getString(2);
            String stringUserHeight = c.getString(3);
            String stringUserWeight = c.getString(4);
            String stringUserActivity = c.getString(5);

            //Age
            String[] dob = stringUserDOB.split("/");
            String stringDate = dob[0];
            String stringMonth = dob[1];
            String stringYear = dob[2];
            int intDate =0, intMonth =0, intYear =0, intUserAge=0;
            try{
                intDate = Integer.parseInt(stringDate);
            }catch (NumberFormatException e){
                System.out.println("Could not parse" + e);
            }
            try{
                intMonth = Integer.parseInt(stringMonth);
            }catch (NumberFormatException e){
                System.out.println("Could not parse" + e);
            }
            try{
                intYear = Integer.parseInt(stringYear);
            }catch (NumberFormatException e){
                System.out.println("Could not parse" + e);
            }

            String stringUserAge = getAge(intDate, intMonth, intYear);
            try{
                intUserAge = Integer.parseInt(stringUserAge);
            }catch (NumberFormatException e){
                System.out.println("Could not parse" + e);
            }

            //Height
            double doubleHeight = 0;
            try{
                doubleHeight = Double.parseDouble(stringUserHeight);
            }catch (NumberFormatException e){
                System.out.println("Could not parse" + e);
            }

            //Weight
            double doubleWeight = 0;
            try{
                doubleWeight = Double.parseDouble(stringUserWeight);
            }catch (NumberFormatException e){
                System.out.println("Could not parse" + e);
            }

            //1.BMR
            double bmr = 0;
            if(stringUserGender.startsWith("m")){
                //male
                // BMR 66.5 + (13.75 x kg body weight) + (5.003 x height in cm) - (6.755 x age)
                bmr = 66.5 + (13.75 * doubleWeight) + (5.003 * doubleHeight) - (6.775 * intUserAge);
                //bmr= Math.round(bmr);
            }
            else{
                //female
                // BMR = 55.1 + (9.563 x kg body weight) + (1.850 x height in cm) - (4.676 x age)
                bmr = 55.1 + (9.563 * doubleWeight) + (1.850 * doubleHeight) - (4.676 * intUserAge);
                //bmr= Math.round(bmr);
            }

            long goalID =1;
            double energyBMRSQL = db.quoteSmart(bmr);
            db.update("goals", "_id", goalID, "goal_energy_bmr", energyBMRSQL) ;

            // 20-25 % protein
            // 40-50 % carb
            // 25-35 % fat
            double proteinsBmr = Math.round(bmr * 25/100);
            double carbsBmr = Math.round(bmr * 50/100);
            double fatBmr = Math.round(bmr * 25/100);

            double proteinBmrSQL = db.quoteSmart(proteinsBmr);
            double carbBmrSQL = db.quoteSmart(carbsBmr);
            double fatBmrSQL = db.quoteSmart(fatBmr);
            db.update("goals", "_id", goalID, "goal_proteins_bmr", proteinBmrSQL);
            db.update("goals", "_id", goalID, "goal_carbs_bmr", carbBmrSQL);
            db.update("goals", "_id", goalID, "goal_fat_bmr", fatBmrSQL);

            //2.with diet
            //loose or gian weight
            double doubleWeeklyGoal = 0;
            try {
                doubleWeeklyGoal = Double.parseDouble(weekly);
            }
            catch(NumberFormatException e){
                System.out.println("Could not parse" + e);
            }
            //1 kg fat == 7700 kcal
            double kcal =0;
            double energyDiet = 0;
            kcal = 7700 * doubleWeeklyGoal;
            if(intIWantTo == 0){ //loose
                energyDiet = Math.round(bmr - (kcal/7));
            }
            else{ //gain
                energyDiet = Math.round(bmr + (kcal/7));
            }

            //update
            double energyDietSQL = db.quoteSmart(energyDiet);
            db.update("goals", "_id", goalID, "goal_energy_diet", energyDietSQL) ;

            // 20-25 % protein
            // 40-50 % carb
            // 25-35 % fat
            double proteinsDiet = Math.round(energyDiet * 25/100);
            double carbsDiet = Math.round(energyDiet * 50/100);
            double fatDiet = Math.round(energyDiet * 25/100);

            double proteinDietSQL = db.quoteSmart(proteinsDiet);
            double carbDietSQL = db.quoteSmart(carbsDiet);
            double fatDietSQL = db.quoteSmart(fatDiet);
            db.update("goals", "_id", goalID, "goal_proteins_diet", proteinDietSQL);
            db.update("goals", "_id", goalID, "goal_carbs_diet", carbDietSQL);
            db.update("goals", "_id", goalID, "goal_fat_diet", fatDietSQL);

            //3.with activity
            double energyWithActivity  = 0;
            if(stringUserActivity.equals("0")){
                energyWithActivity = bmr*1.2;
            }
            if(stringUserActivity.equals("1")){
                energyWithActivity = bmr*1.375;
            }
            if(stringUserActivity.equals("2")){
                energyWithActivity = bmr*1.55;
            }
            if(stringUserActivity.equals("3")){
                energyWithActivity = bmr*1.725;
            }
            energyWithActivity = Math.round(energyWithActivity);
            double energyWithActivitySQL = db.quoteSmart(energyWithActivity);
            db.update("goals", "_id", goalID, "goal_energy_with_activity", energyWithActivitySQL) ;

            // 20-25 % protein
            // 40-50 % carb
            // 25-35 % fat
            double proteinsWithActivity = Math.round(energyWithActivity * 25/100);
            double carbsWithActivity = Math.round(energyWithActivity * 50/100);
            double fatWithActivity = Math.round(energyWithActivity * 25/100);

            double proteinActivitySQL = db.quoteSmart(proteinsWithActivity);
            double carbActivitySQL = db.quoteSmart(carbsWithActivity);
            double fatActivitySQL = db.quoteSmart(fatWithActivity);
            db.update("goals", "_id", goalID, "goal_proteins_with_activity", proteinActivitySQL);
            db.update("goals", "_id", goalID, "goal_carbs_with_activity", carbActivitySQL);
            db.update("goals", "_id", goalID, "goal_fat_with_activity", fatActivitySQL);


            //4. activity and diet
            //1 kg fat == 7700 kcal
            kcal =0;
            double energyWithActivityAndDiet = 0;
            kcal = 7700 * doubleWeeklyGoal;
            if(intIWantTo == 0){ //loose
                energyWithActivityAndDiet = Math.round(bmr - (kcal/7));
            }
            else{ //gain
                energyWithActivityAndDiet = Math.round(bmr + (kcal/7));
            }

            //update
            double energyWithActivityAndDietSQL = db.quoteSmart(energyWithActivityAndDiet);
            db.update("goals", "_id", goalID, "goal_energy_with_activity_and_diet", energyWithActivityAndDietSQL) ;

            //calculate % proteins
            // 20-25 % protein
            // 40-50 % carb
            // 25-35 % fat
            double proteins = Math.round(energyWithActivityAndDiet * 25/100);
            double carbs = Math.round(energyWithActivityAndDiet * 50/100);
            double fat = Math.round(energyWithActivityAndDiet * 25/100);

            double proteinSQL = db.quoteSmart(proteins);
            double carbSQL = db.quoteSmart(carbs);
            double fatSQL = db.quoteSmart(fat);
            db.update("goals", "_id", goalID, "goal_proteins_with_activity_and_diet", proteinSQL);
            db.update("goals", "_id", goalID, "goal_carbs_with_activity_and_diet", carbSQL);
            db.update("goals", "_id", goalID, "goal_fat_with_activity_and_diet", fatSQL);
        }

        db.close();
        if(errorMessGoal.isEmpty()) {
            Intent i = new Intent(SignUpGoal.this, MainActivity.class);
            startActivity(i);
        }
        else{
            tvErrorMessGoal.setText(errorMessGoal);
        }
    }

    private String getAge (int day, int month, int year){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(day, month, year);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();
        return ageS;

    }

    private void initView(){
        edTarget = findViewById(R.id.editTextTargerWeight);
        edWeekly = findViewById(R.id.editTextWeekly);
        spWeekly = findViewById(R.id.spinnerWeekly);
        btnLetsStart = findViewById(R.id.btnLetsStart);
    }
}
