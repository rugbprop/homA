
package st.alr.homA.preferences;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import st.alr.homA.ActivityPreferences;
import st.alr.homA.R;
import st.alr.homA.services.ServiceMqtt;
import st.alr.homA.support.Defaults;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class PreferencesBroker extends DialogPreference {
    private Context context;
    private EditText host;
    private EditText port;
    private EditText password;

    private static EditText brokerUsername;

    
    private EditText brokerSecuritySSLCaCrtPath;

    private Spinner brokerSecurity;
    private View brokerSecuritySSLOptions;
    private View brokerSecurityNoneOptions;
    private Spinner brokerAuth;
    
    private LinearLayout securityWrapper;
    private LinearLayout brokerUsernameWrapper;
    private LinearLayout brokerPasswordWrapper;
    private LinearLayout brokerAuthWrapper;
    
    
    
    
    private enum RequireablePreferences { BROKER_HOST, BROKER_PORT, BROKER_USERNAME, BROKER_PASSWORD, CACRT};
    
    Set<RequireablePreferences> okPreferences = Collections.synchronizedSet(EnumSet.noneOf(RequireablePreferences.class));
    Set<RequireablePreferences> requiredPreferences = Collections.synchronizedSet(EnumSet.of(RequireablePreferences.BROKER_HOST));

    public PreferencesBroker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        setDialogLayoutResource(R.layout.preferences_broker);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }

    @Override
    protected View onCreateDialogView() {
        View root = super.onCreateDialogView();

        securityWrapper = (LinearLayout) root.findViewById(R.id.securityWrapper);
        brokerUsernameWrapper = (LinearLayout) root.findViewById(R.id.brokerUsernameWrapper);
        brokerPasswordWrapper = (LinearLayout) root.findViewById(R.id.brokerPasswordWrapper);
        brokerAuthWrapper = (LinearLayout) root.findViewById(R.id.brokerAuthWrapper);

        

        
        host = (EditText) root.findViewById(R.id.brokerHost);
        port = (EditText) root.findViewById(R.id.brokerPort);

        brokerUsername = (EditText) root.findViewById(R.id.brokerUsername);
        password = (EditText) root.findViewById(R.id.brokerPassword);
        brokerSecurity = (Spinner) root.findViewById(R.id.brokerSecurity);
        brokerAuth = (Spinner) root.findViewById(R.id.brokerAuth);

        brokerSecurityNoneOptions = root.findViewById(R.id.brokerSecurityNoneOptions);
        brokerSecuritySSLOptions = root.findViewById(R.id.brokerSecuritySSLOptions);
        brokerSecuritySSLCaCrtPath = (EditText) root.findViewById(R.id.brokerSecuritySSLCaCrtPath);        
        
        return root;
    }


    @Override
    protected void onBindDialogView(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        host.setText(prefs.getString(Defaults.SETTINGS_KEY_BROKER_HOST, ""));
        port.setText(prefs.getString(Defaults.SETTINGS_KEY_BROKER_PORT, ""));
        port.setHint(Defaults.VALUE_BROKER_PORT);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        brokerUsername.setText(ActivityPreferences.getBrokerUsername());
        password.setText(prefs.getString(Defaults.SETTINGS_KEY_BROKER_PASSWORD, ""));

        int visibility = p.getInt(Defaults.SETTINGS_KEY_BROKER_AUTH, Defaults.VALUE_BROKER_AUTH_ANONYMOUS) == Defaults.VALUE_BROKER_AUTH_BROKERUSERNAME ? View.VISIBLE : View.GONE;
        Log.v(this.toString(), "setting selection to " + p.getInt(Defaults.SETTINGS_KEY_BROKER_AUTH, Defaults.VALUE_BROKER_AUTH_ANONYMOUS));
        brokerAuth.setSelection(p.getInt(Defaults.SETTINGS_KEY_BROKER_AUTH, Defaults.VALUE_BROKER_AUTH_ANONYMOUS));
        brokerUsernameWrapper.setVisibility(visibility);
        brokerPasswordWrapper.setVisibility(visibility);

        brokerSecurity.setSelection(PreferenceManager.getDefaultSharedPreferences(context).getInt(Defaults.SETTINGS_KEY_BROKER_SECURITY, Defaults.VALUE_BROKER_SECURITY_SSL));        
        brokerSecuritySSLCaCrtPath.setText(prefs.getString(Defaults.SETTINGS_KEY_BROKER_SECURITY_SSL_CA_PATH, ""));
        brokerSecuritySSLOptions.setVisibility(p.getInt(Defaults.SETTINGS_KEY_BROKER_SECURITY, Defaults.VALUE_BROKER_SECURITY_NONE) == Defaults.VALUE_BROKER_SECURITY_SSL_CUSTOMCACRT ? View.VISIBLE : View.GONE);
    }

    
    
    
    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        handleHost();
        handlePort();       
        handleBrokerAuth();
        handleUsername();
        handleBrokerSecurity();
        handleCaCrt();        

        
               
        

        conditionalyEnableConnectButton();
        conditionallyEnableDisconnectButton();

        
        
        
        
        TextWatcher hostPortWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleHost();
                handlePort();
                conditionalyEnableConnectButton();
            }
        };
        
        host.addTextChangedListener(hostPortWatcher);
        port.addTextChangedListener(hostPortWatcher);

        brokerSecurity.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                handleBrokerSecurity();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                brokerSecurity.setSelection(Defaults.VALUE_BROKER_SECURITY_NONE);
            }
        });
 
        
        brokerAuth.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                handleBrokerAuth();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                brokerAuth.setSelection(Defaults.VALUE_BROKER_AUTH_ANONYMOUS);
            }
        });
        brokerUsername.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleUsername();
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    
    private void handleCaCrt() {
        handleState(RequireablePreferences.CACRT, brokerSecuritySSLCaCrtPath.getText().toString().length() > 0);
        
    }

    private void handleBrokerAuth() {
        switch (brokerAuth.getSelectedItemPosition()) {
            case Defaults.VALUE_BROKER_AUTH_BROKERUSERNAME:
                Log.v(this.toString(), "auth username");
                brokerUsernameWrapper.setVisibility(View.VISIBLE);
                brokerPasswordWrapper.setVisibility(View.VISIBLE);
                requiredPreferences.add(RequireablePreferences.BROKER_USERNAME);
                break;
            default:
                Log.v(this.toString(), "auth anon");

                brokerUsernameWrapper.setVisibility(View.GONE);
                brokerPasswordWrapper.setVisibility(View.GONE);
                requiredPreferences.remove(RequireablePreferences.BROKER_USERNAME);
                break;

        }        
        conditionalyEnableConnectButton();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: // Clicked connect

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(Defaults.SETTINGS_KEY_BROKER_HOST, host.getText().toString());
                editor.putString(Defaults.SETTINGS_KEY_BROKER_PORT, port.getText().toString());
                editor.putString(Defaults.SETTINGS_KEY_BROKER_USERNAME, brokerUsername.getText().toString());
                editor.putString(Defaults.SETTINGS_KEY_BROKER_PASSWORD, password.getText().toString());
                editor.putInt(Defaults.SETTINGS_KEY_BROKER_SECURITY, brokerSecurity.getSelectedItemPosition());
                editor.putInt(Defaults.SETTINGS_KEY_BROKER_AUTH, brokerAuth.getSelectedItemPosition());

                editor.putString(Defaults.SETTINGS_KEY_BROKER_SECURITY_SSL_CA_PATH, brokerSecuritySSLCaCrtPath.getText().toString());
                

                editor.apply()
                
                ;
                Runnable r = new Runnable() {
                    
                    @Override
                    public void run() {
                        ServiceMqtt.getInstance().reconnect();                        
                    }
                };
                new Thread( r ).start();

                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Runnable s = new Runnable() {
                    
                    @Override
                    public void run() {
                        ServiceMqtt.getInstance().disconnect(true);
                        
                    }
                };
                new Thread( s ).start();
        }
        super.onClick(dialog, which);
    }
    
    private void handleState(RequireablePreferences p, boolean ok) {
        if(ok)
            okPreferences.add(p);
        else
            okPreferences.remove(p);
        
        conditionalyEnableConnectButton();
    }

    private void handleUsername() {
        try {
            handleState(RequireablePreferences.BROKER_USERNAME, brokerUsername.getText().toString().length() > 0);
        } catch (Exception e) {
            handleState(RequireablePreferences.BROKER_USERNAME, false);
        }
    }
    private void handleHost() {
        try {
            handleState(RequireablePreferences.BROKER_HOST, host.getText().toString().length() > 0);
        } catch (Exception e) {
            handleState(RequireablePreferences.BROKER_HOST, false);
        }
    }

    private void handlePort() {
        try {            
            Integer p = Integer.parseInt(port.getText().toString());
            handleState(RequireablePreferences.BROKER_PORT, (p > 0) && (p <= 65535));
        } catch (Exception e) {
            handleState(RequireablePreferences.BROKER_PORT, false);
        }
    }
    
    
    private void conditionalyEnableConnectButton() {
        View v = getDialog().findViewById(android.R.id.button1);
        if (v == null)
            return;
                
        Log.v("Required for connect: ", requiredPreferences.toString() );
        Log.v("Currently set",okPreferences.toString());

        v.setEnabled(okPreferences.containsAll(requiredPreferences));
    }
    
    private void conditionallyEnableDisconnectButton() {
        View v = getDialog().findViewById(android.R.id.button2);
        if (v == null)
            return;

        if (ServiceMqtt.getState() == Defaults.State.ServiceMqtt.CONNECTING
                || ServiceMqtt.getState() == Defaults.State.ServiceMqtt.CONNECTED) {
            v.setEnabled(true);
        } else {
            v.setEnabled(false);
        }

    }

    private void handleBrokerSecurity() {
        switch (brokerSecurity.getSelectedItemPosition()) {
            case Defaults.VALUE_BROKER_SECURITY_NONE:
                brokerSecuritySSLOptions.setVisibility(View.GONE);
                brokerSecurityNoneOptions.setVisibility(View.VISIBLE);
                requiredPreferences.remove(RequireablePreferences.CACRT);
                break;
            case Defaults.VALUE_BROKER_SECURITY_SSL_CUSTOMCACRT:
                brokerSecuritySSLOptions.setVisibility(View.VISIBLE);
                brokerSecurityNoneOptions.setVisibility(View.GONE);
                requiredPreferences.add(RequireablePreferences.CACRT);
                break;

            default:
                brokerSecuritySSLOptions.setVisibility(View.GONE);
                brokerSecurityNoneOptions.setVisibility(View.VISIBLE);
                requiredPreferences.remove(RequireablePreferences.CACRT);
                break;
        }
        conditionalyEnableConnectButton();

    }
}
