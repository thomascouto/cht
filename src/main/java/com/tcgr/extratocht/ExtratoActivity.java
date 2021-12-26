package com.tcgr.extratocht;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Extrato Activity
 */
public class ExtratoActivity extends AppCompatActivity {

    private String anac;
    private WebView webView;
    private ProgressDialog pDialog;
    private boolean webViewSuccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extrato);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        anac = intent.getStringExtra(Constants.PREF_ANAC);
        String cpf = intent.getStringExtra(Constants.PREF_CPF);
        boolean isEnglish = intent.getBooleanExtra(Constants.PREF_CHECKED, false);

        webView = (WebView) findViewById(R.id.webview);
        WebSettings settings;

        if (webView != null) {
            settings = webView.getSettings();
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setBuiltInZoomControls(true);
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pDialog = ProgressDialog.show(ExtratoActivity.this, "", getString(R.string.carregando), true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (webViewSuccess) {
                    pDialog.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                webViewSuccess = false;
                if (pDialog.isShowing()) {
                    pDialog.cancel();
                    Toast.makeText(ExtratoActivity.this, getString(R.string.erro), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isEnglish) {
            webView.loadUrl(String.format(Constants.ANAC_EN_URL, anac, cpf));
        } else {
            webView.loadUrl(String.format(Constants.ANAC_PT_URL, anac, cpf));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    /**
     * @param view ...
     * @return Bitmap
     */
    private Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getDrawingCache());
//        Canvas canvas = new Canvas(returnedBitmap);
//        Drawable bgDrawable = view.getBackground();
//        if (bgDrawable != null) {
//            bgDrawable.draw(canvas);
//        } else {
//            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
//        }
//
//        // draw the view on the canvas
//        view.draw(canvas);
        //return the bitmap
        view.setDrawingCacheEnabled(false);
        return returnedBitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                File cachePath = null;

                try {
                    Bitmap cacheBitmap = getBitmapFromView(webView);
                    cachePath = new File(getCacheDir(), "images");
                    cachePath.mkdirs();
                    FileOutputStream stream = new FileOutputStream(cachePath + "/extrato_cht.png");
                    cacheBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (cachePath != null) {
                        File shareFile = new File(cachePath, "extrato_cht.png");
                        Uri contentUri = FileProvider.getUriForFile(getBaseContext(), "com.tcgr.extratocht.fileprovider", shareFile);

                        if (contentUri != null) {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                        }
                    }
                }
                break;

            case R.id.action_save:
                /*
                  @see ExtratoActivity#onRequestPermissionsResult(int, String[], int[])
                 */
                //Verificando permissões...
                if(!hasWritePermission()) {
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(ExtratoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(ExtratoActivity.this, Constants.PERMISSIONS, Constants.PERMISSION_ALL);
                    }
                    return true;
                }

                if(hasWritePermission()) {
                    saveImage();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage();
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @return True if permission is granted, False otherwise.
     */
    private boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void saveImage() {
        Bitmap b = getBitmapFromView(webView);
        Date now = new Date();
        CharSequence c = android.text.format.DateFormat.format("yyyy-MM-dd_HHmmss", now);
        String root = Environment.getExternalStorageDirectory().toString();

        File imageRoot = new File(root, Constants.APP_DIRECTORY_NAME);
        imageRoot.mkdirs();
        File imageFile = new File(imageRoot, anac + "_" + c.toString() + Constants.PNG);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, Constants.IMAGE);
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        } catch (IOException ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.cancel();
        }
        webView.clearCache(true);
    }
}