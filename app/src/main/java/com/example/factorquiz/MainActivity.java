package com.example.factorquiz;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    int number;
    int longestStreak;
    int currentStreak;
    List<Integer> factors = new ArrayList<Integer>();
    List<Integer> others = new ArrayList<Integer>();
    int[] options = new int[3];
    int answer;
    Button optionOneButton;
    Button optionTwoButton;
    Button optionThreeButton;
    Button resetButton;
    int layout = 0;
    int selected = -1;
    boolean hasVibrated = false;
    Vibrator vibrator;
    CountDownTimer countDownTimer;
    long timeLeftInMillis = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLongestStreak();
        if (savedInstanceState != null){
            number = savedInstanceState.getInt("number");
            answer = savedInstanceState.getInt("answer");
            options = savedInstanceState.getIntArray("options");
            layout = savedInstanceState.getInt("layout");
            currentStreak = savedInstanceState.getInt("current_streak");
            selected = savedInstanceState.getInt("selected");
            hasVibrated = savedInstanceState.getBoolean("has_vibrated");
            timeLeftInMillis = savedInstanceState.getLong("time_left_in_millis");
        }
        if (layout == 1){
            setContentView(R.layout.activity_2);
            displayQuestion();
            displayOptions();
            startTimer();
            updateCountDownText();
            if (selected >= 0){
                if (selected == answer){
                    currentStreak -= 1;
                }
                selectSavedOption(selected);
            }
            displayStreaks();
        }
        displayStreaks();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("layout", layout);
        outState.putIntArray("options", options);
        outState.putInt("answer", answer);
        outState.putInt("current_streak", currentStreak);
        outState.putInt("number", number);
        outState.putInt("selected", selected);
        outState.putBoolean("has_vibrated", hasVibrated);
        outState.putLong("time_left_in_millis", timeLeftInMillis);
    }

    public void submitNumber(View view){
        TextView numberTextView = (TextView) findViewById(R.id.number_text_view);
        if (numberTextView.getText().toString().equals("")){
            return;
        }
        number = Integer.parseInt(numberTextView.getText().toString());
        if(number <=0){
            TextView enterNumberText = (TextView) findViewById(R.id.enter_number_warning_text_view);
            enterNumberText.setVisibility(View.VISIBLE);
            return;
        }
        setContentView(R.layout.activity_2);
        layout = 1;
        displayQuestion();
        calculateOptions();
        displayOptions();
        displayStreaks();
        startTimer();
    }

    private void displayQuestion(){
        TextView questionTextView = (TextView) findViewById(R.id.question_text_view);
        questionTextView.setText("Pick the factor of " + number + " from the following");
    }

    private void displayOptions(){
        resetButton = (Button) findViewById(R.id.reset_button);
        optionOneButton = (Button) findViewById(R.id.option_one_button);
        optionTwoButton = (Button) findViewById(R.id.option_two_button);
        optionThreeButton = (Button) findViewById(R.id.option_three_button);
        optionOneButton.setText(""+options[0]);
        optionTwoButton.setText(""+options[1]);
        optionThreeButton.setText(""+options[2]);
    }

    private void calculateOptions(){
        calculateFactors();
        Random rand = new Random();
        options[0] = factors.get(rand.nextInt(factors.size()));
        while(options[1] == options[2]){
            options[1] = others.get(rand.nextInt(others.size()));
            options[2] = others.get(rand.nextInt(others.size()));
        }
        int temp;
        answer = rand.nextInt(3);
        switch(answer){
            case 1:
                temp = options[0];
                options[0] = options[1];
                options[1] = temp;
                break;
            case 2:
                temp = options[0];
                options[0] = options[2];
                options[2] = temp;
                break;
        }
    }

    private void calculateFactors(){
        for(int i = 1; i <= number; i++){
            if(number % i == 0){
                factors.add(i);
            }
            else{
                others.add(i);
            }
        }
        factors.add(number);
        others.add(0);
        others.add(number+1);
    }

    public void reset(View view){
        factors.clear();
        others.clear();
        setContentView(R.layout.activity_main);
        displayStreaks();
        selected = -1;
        layout = 0;
        hasVibrated = false;
        timeLeftInMillis = 10000;
        countDownTimer.cancel();
        options[0] = options[1] = options[2] = 0;
    }

    private void displayStreaks(){
        if (currentStreak > longestStreak){
            longestStreak = currentStreak;
            saveLongestStreak();
        }
        TextView longestStreakTextView = (TextView) findViewById(R.id.longest_streak_text_view);
        TextView currentStreakTextView = (TextView) findViewById(R.id.current_streak_text_view);
        longestStreakTextView.setText("Longest Streak: " + longestStreak);
        currentStreakTextView.setText("Current Streak: " + currentStreak);
    }

    private void saveLongestStreak(){
        SharedPreferences sharedPref = getSharedPreferences("quizInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("longest_streak", longestStreak);
        editor.apply();
    }

    private void getLongestStreak(){
        SharedPreferences sharedPref = getSharedPreferences("quizInfo", Context.MODE_PRIVATE);
        longestStreak = sharedPref.getInt("longest_streak", 0);
    }


    public void selectOption(View view){
        countDownTimer.cancel();
        if (view.getId() == optionOneButton.getId()){
            selected = 0;
        }
        else if(view.getId() == optionTwoButton.getId()){
            selected = 1;
        }
        else{
            selected = 2;
        }
        optionOneButton.setClickable(false);
        optionTwoButton.setClickable(false);
        optionThreeButton.setClickable(false);
        switch (answer){
            case 0:
                optionOneButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                break;
            case 1:
                optionTwoButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                break;
            case 2:
                optionThreeButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                break;
        }
        if (answer == selected){
            currentStreak += 1;
        }
        else{
            view.setBackgroundColor(getResources().getColor(R.color.failureRed));
            if (!hasVibrated){
                hasVibrated = true;
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150,200));
                }
                else {
                    vibrator.vibrate(150);
                }
            }
            currentStreak = 0;
        }
        displayStreaks();
    }

    private void selectSavedOption(int selected){
        switch (selected){
            case 0:
                selectOption(optionOneButton);
                break;
            case 1:
                selectOption(optionTwoButton);
                break;
            case 2:
                selectOption(optionThreeButton);
        }
    }

    private void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                stopGame();
            }
        }.start();
    }

    private void updateCountDownText(){
        TextView timerTextView = (TextView) findViewById(R.id.timer_text_view);
        timerTextView.setText("Time Left\n" + (timeLeftInMillis/1000));
    }

    private void stopGame(){
        TextView timerTextView = (TextView) findViewById(R.id.timer_text_view);
        timerTextView.setText("Time Over");
        optionOneButton.setClickable(false);
        optionTwoButton.setClickable(false);
        optionThreeButton.setClickable(false);
        optionOneButton.setBackgroundColor(getResources().getColor(R.color.failureRed));
        optionTwoButton.setBackgroundColor(getResources().getColor(R.color.failureRed));
        optionTwoButton.setBackgroundColor(getResources().getColor(R.color.failureRed));
        switch (answer){
            case 0:
                optionOneButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                break;
            case 1:
                optionTwoButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                break;
            case 2:
                optionThreeButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                break;
        }
        currentStreak = 0;
        displayStreaks();
    }


    /*public void optionOne(View view){
        optionOneButton.setClickable(false);
        optionTwoButton.setClickable(false);
        optionThreeButton.setClickable(false);
        if (answer == 0){
            optionOneButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            currentStreak += 1;
        }
        else{
            optionOneButton.setBackgroundColor(getResources().getColor(R.color.failureRed));
            currentStreak = 0;
            if(answer == 1){
                optionTwoButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            }
            else{
                optionThreeButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            }
        }
        displayStreaks();
    }

    public void optionTwo(View view){
        optionOneButton.setClickable(false);
        optionTwoButton.setClickable(false);
        optionThreeButton.setClickable(false);
        if (answer == 1){
            optionTwoButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            currentStreak += 1;
        }
        else{
            optionTwoButton.setBackgroundColor(getResources().getColor(R.color.failureRed));
            currentStreak = 0;
            if(answer == 0){
                optionOneButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            }
            else{
                optionThreeButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            }
        }
        displayStreaks();;
    }

    public void optionThree(View view){
        optionOneButton.setClickable(false);
        optionTwoButton.setClickable(false);
        optionThreeButton.setClickable(false);
        if (answer == 2){
            optionThreeButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            currentStreak += 1;
        }
        else{
            optionThreeButton.setBackgroundColor(getResources().getColor(R.color.failureRed));
            currentStreak = 0;
            if(answer == 0){
                optionOneButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            }
            else{
                optionTwoButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
            }
        }
        displayStreaks();
    }*/


    //OLD SELECT OPTION
    /*switch (answer){
            case 0:
                optionOneButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                if (view.getId() != optionOneButton.getId()){
                    view.setBackgroundColor(getResources().getColor(R.color.failureRed));
                    currentStreak = 0;
                }
                else{
                    currentStreak += 1;
                }
                break;
            case 1:
                optionTwoButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                if (view.getId() != optionTwoButton.getId()){
                    view.setBackgroundColor(getResources().getColor(R.color.failureRed));
                    currentStreak = 0;
                }
                else{
                    currentStreak += 1;
                }
                break;
            case 2:
                optionThreeButton.setBackgroundColor(getResources().getColor(R.color.sucessGreen));
                if (view.getId() != optionThreeButton.getId()){
                    view.setBackgroundColor(getResources().getColor(R.color.failureRed));
                    currentStreak = 0;
                }
                else{
                    currentStreak += 1;
                }
                break;

        }*/
}
