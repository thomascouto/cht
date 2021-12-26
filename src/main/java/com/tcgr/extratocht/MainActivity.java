package com.tcgr.extratocht;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main Activity
 *
 * @see AppCompatActivity
 */
public class MainActivity extends AppCompatActivity {

    private EditText canac, cpf;
    private CheckBox isEnglish, save;
    private Button sendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cpf = (EditText) findViewById(R.id.cpf_edit_text);
        canac = (EditText) findViewById(R.id.canac_edit_text);
        isEnglish = (CheckBox) findViewById(R.id.checkbox_ingles);
        save = (CheckBox) findViewById(R.id.checkbox_salvar);
        sendData = (Button) findViewById(R.id.enviar_button);

        MaskEditTextChangedListener maskCpf = new MaskEditTextChangedListener("###.###.###-##", cpf);

        //Preferences
        SharedPreferences p = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        boolean isChecked = p.getBoolean(Constants.PREF_CHECKED, false);
        canac.setText(p.getString(Constants.PREF_ANAC, ""));
        cpf.setText(p.getString(Constants.PREF_CPF, ""));
        cpf.addTextChangedListener(maskCpf);
        isEnglish.setChecked(isChecked);

        int cursor = canac.getText().length();
        if (cursor > 0) {
            canac.setSelection(cursor);
        }

        canac.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    cpf.setText(null);
                }
            }
        });

        cpf.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendData.performClick();
                    return true;
                }
                return false;
            }
        });

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent i = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(i);
        }
        return false;
    }

    /**
     * Envia o formulario
     */
    private void sendData() {
        boolean isEnglishChecked = isEnglish.isChecked();
        String sCanac = canac.getText().toString();
        String sCpf = cpf.getText().toString();

        if (save.isChecked()) {
            getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(Constants.PREF_ANAC, sCanac)
                    .putString(Constants.PREF_CPF, sCpf)
                    .putBoolean(Constants.PREF_CHECKED, isEnglishChecked)
                    .apply();
        }

        sCpf = sCpf.replaceAll("[.-]", "");

        if (sCanac.length() == 6 && isValidCPF(sCpf)) {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(this, ExtratoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.PREF_ANAC, sCanac);
                intent.putExtra(Constants.PREF_CPF, sCpf);
                intent.putExtra(Constants.PREF_CHECKED, isEnglishChecked);
                startActivity(intent);
            } else {
                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.conexao))
                        .setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                moveTaskToBack(true);
                            }
                        }).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.dados_incorretos), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Verifica se há conexão ativa de internet.
     *
     * @return true ou false
     */
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private int calcularDigito(String str, int[] peso) {
        int soma = 0;
        for (int indice = str.length() - 1, digito; indice >= 0; indice--) {
            digito = Integer.parseInt(str.substring(indice, indice + 1));
            soma += digito * peso[peso.length - str.length() + indice];
        }
        soma = 11 - soma % 11;
        return soma > 9 ? 0 : soma;
    }

    private boolean isValidCPF(String cpf) {
        int[] pesoCPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        if ((cpf == null) || (cpf.length() != 11)) return false;

        Integer digito1 = calcularDigito(cpf.substring(0, 9), pesoCPF);
        Integer digito2 = calcularDigito(cpf.substring(0, 9) + digito1, pesoCPF);
        return cpf.equals(cpf.substring(0, 9) + digito1.toString() + digito2.toString());
    }
}