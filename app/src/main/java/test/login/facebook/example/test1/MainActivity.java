package test.login.facebook.example.test1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    CallbackManager callbackManager;
    TextView txtName, txtEmail, txtBirthday, txtFriends, txtLocation;
    ProgressDialog mDialog;
    ImageView imgAvatar;
    LocationManager locationManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();
        txtBirthday = (TextView) findViewById(R.id.txtBirthday);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtFriends = (TextView) findViewById(R.id.txtFriends);
        imgAvatar = (ImageView) findViewById(R.id.avatar);
        txtLocation = (TextView) findViewById(R.id.txtLocation);

        // Login Button

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));

        // Location Manager to access the location and permission.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        onLocationChanged(location);

        //Getting the permission for receiving the data from the facebook using Access Tokens.

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage("Retrieving data...");
                mDialog.show();

                String accesstoken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
                        Log.d("response", response.toString());
                        getData(object);
                        //Bundle facebookdata = getFacebookData(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,birthday,friends");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


        //If already login
        if (AccessToken.getCurrentAccessToken() != null) {
            //Just set User Id
            txtEmail.setText(AccessToken.getCurrentAccessToken().getUserId());
        }
    }

    //Getting the data and displying it in the Application\

    private void getData(JSONObject object) {
        try {
            URL profile_picture = new URL("https://graph.facebook.com/" + object.getString("id") + "/picture?width=250&height=250");
            Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);

            txtEmail.setText("Email: " + object.getString("email"));
            txtBirthday.setText("Birthday: " + object.getString("birthday"));
            txtFriends.setText("Friends: " + object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Get the latitute and longitute using the GPS.

    @Override
    public void onLocationChanged(Location location) {
             double longtitute = location.getLongitude();
        double latitute = location.getLatitude();
        String cityName = getCityName(longtitute, latitute);
    }

    //Get the City Name from the latitute and longitute.

    private String getCityName(double longtitute, double latitute) {
        String myCity = "";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitute, longtitute, 1);
            String address = addresses.get(0).getAddressLine(0);
            myCity = addresses.get(0).getLocality();
            Log.d("mylog", "Complete Address: " + addresses.toString());
            Log.d("mylog", "Address: " + address);
            txtLocation.setText("Current Location: " + myCity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCity;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
