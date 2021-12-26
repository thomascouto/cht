package com.tcgr.extratocht;

import android.Manifest;

/**
 * Constantes
 *
 * Created by thomas on 03/03/16.
 */
interface Constants {
    String PREFS_NAME = "ExtratoCHTPrefFile";
    String PREF_ANAC = "anac";
    String PREF_CPF = "cpf";
    String PREF_CHECKED = "isChecked";
    String APP_DIRECTORY_NAME = "Extrato_CHT";
    String ANAC_EN_URL = "http://www2.anac.gov.br/consultasdelicencas/imp_licencas_en.asp?nf=%s&cpf=%s";
    String ANAC_PT_URL = "http://www2.anac.gov.br/consultasdelicencas/imp_licencas.asp?nf=%s&cpf=%s";
    String IMAGE = "image/png";
    String PNG = ".png";
    String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    int PERMISSION_ALL = 1;
}