package ca.kwisses.saveandquit.main;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

import ca.kwisses.saveandquit.R;
import ca.kwisses.saveandquit.check_in.CheckInActivity;
import ca.kwisses.saveandquit.db_handler.DBHandler;
import ca.kwisses.saveandquit.user.User;

public class MainPresenter implements MainContract.Presenter {

    private MainContract.MvpView mvpView;

    private Context context;

    private DBHandler dbHandler;

    // Initial variables
    TextView quoteText;
    TextView moneySavedField;
    TextView extraLifeField;
    Button checkInButton;

    // User input data
    private String[] userData;
    double cigPackCost;
    int cigsInPack;
    int cigsPerDay;
    int days;

    // Calculated user data
    double costPerDay;
    double moneySaved;
    double extraLife;

    // Time (in minutes) it takes to smoke 1 cigarette
    static final int SMOKING_TIME = 6;

    MainPresenter(MainContract.MvpView view, Context context) {
        mvpView = view;
        this.context = context;
    }

    @Override
    public void init(View view) {
        setQuoteText((TextView) view.findViewById(R.id.quoteText));
        setMoneySavedField((TextView) view.findViewById(R.id.moneySavedField));
        setExtraLifeField((TextView) view.findViewById(R.id.extraLifeField));
        setCheckInButton((Button) view.findViewById(R.id.checkInButton));
    }

    @Override
    public String getQuoteTextFromFile() {
        String[] array = context.getResources().getStringArray(R.array.quotes);
        int n = new Random().nextInt(array.length);
        return array[n];
    }

    @Override
    public void loadUser() {
        String[] nullArray = {null, null, null, null, null};
        try {
            String[] dbArray = dbHandler.getUserDataFromDatabase();
            if(Arrays.equals(dbArray, nullArray)) {
                createNewUser(dbHandler);
            } else {
                userData = getUserData();
                parseUserData(userData);
                setUser();
                setCalculations();
            }
        } catch(NullPointerException e) {
            throw new NullPointerException();
        }
    }

    @Override
    public void createNewUser(DBHandler dbHandler) {
        int[] intArray = {1, 0, 0, 0, 0};
        MainActivity.user = new User(intArray);
        try {
            dbHandler.addUser(MainActivity.user);
        }catch (NullPointerException e) {
            throw new NullPointerException();
        }
    }

    @Override
    public String[] getUserData() {
        String[] data;

        try {
            data = dbHandler.getUserDataFromDatabase();
        } catch (NullPointerException e) {
            throw new NullPointerException();
        }

        return data;
    }

    @Override
    public void parseUserData(String[] userData) {
        setCigPackCost(Double.parseDouble(userData[1]));
        setCigsInPack(Integer.parseInt(userData[2]));
        setCigsPerDay(Integer.parseInt(userData[3]));
        setDays(Integer.parseInt(userData[4]));
    }

    @Override
    public void setUser() {
        MainActivity.user = new User(1, cigPackCost, cigsInPack, cigsPerDay, days);
    }


    @Override
    public void setCalculations() {
        try {
            costPerDay = cigsPerDay / (float) cigsInPack * cigPackCost;
            if(Double.isNaN(costPerDay)) {
                costPerDay = 0;
            }
            moneySaved = costPerDay * days;
            extraLife = (cigsPerDay * (SMOKING_TIME / 60.0f) * days);
        } catch (ArithmeticException e) {
            resetCalculations();
        }
    }

    @Override
    public void resetAllData() {
        resetUserData();
        resetCalculations();
    }

    @Override
    public void resetUserData() {
        cigPackCost = 0;
        cigsInPack = 0;
        cigsPerDay = 0;
        days = 0;
    }

    @Override
    public void resetCalculations() {
        costPerDay = 0;
        moneySaved = 0;
        extraLife = 0;
    }

    @Override
    public void updateUser() {
        dbHandler.deleteUser(MainActivity.user);
        MainActivity.user = new User(1, cigPackCost, cigsInPack, cigsPerDay, days + 1);
        dbHandler.addUser(MainActivity.user);
    }

    @Override
    public void setPresenterData() {
        userData = getUserData();
        parseUserData(userData);
        setCalculations();
    }

    @Override
    public void onCheckInButton(View view) {
        String[] strArray = {"1", "0.0", "0", "0", "0"};

        if(!Arrays.equals(dbHandler.getUserDataFromDatabase(), strArray)) {
            if(context != null) {
                Toast.makeText(context, R.string.checked_in, Toast.LENGTH_LONG).show();
            }

            updateUser();
            setPresenterData();

            mvpView.setDisplayText(moneySaved, extraLife);
            checkInButton.setEnabled(false);
        } else {
            Intent i = new Intent(context, CheckInActivity.class);
            context.startActivity(i);
        }
    }

    // Getters and Setters

    @Override
    public DBHandler getDBHandler() {
        return dbHandler;
    }

    @Override
    public void setDBHandler(DBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Override
    public void setUserData(String[] userData) {
        this.userData = userData;
    }

    @Override
    public void setQuoteText(TextView textView) {
        quoteText = textView;
    }

    @Override
    public void setMoneySavedField(TextView textView) {
        moneySavedField = textView;
    }

    @Override
    public void setExtraLifeField(TextView textView) {
        extraLifeField = textView;
    }

    @Override
    public void setCheckInButton(Button button) {
        checkInButton = button;
    }

    @Override
    public void setCigPackCost(double cigPackCost) {
        this.cigPackCost = cigPackCost;
    }

    @Override
    public void setCigsInPack(int cigsInPack) {
        this.cigsInPack = cigsInPack;
    }

    @Override
    public void setCigsPerDay(int cigsPerDay) {
        this.cigsPerDay = cigsPerDay;
    }

    @Override
    public void setDays(int days) {
        this.days = days;
    }
}
