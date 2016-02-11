package com.raparthiss.cammapapp;

import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int SELECT_FILE = 200;
    private ImageView imageButton;
    private EditText firstNameCtrl;
    private EditText lastNameCtrl;
    private EditText addressCtrl;
    public Bitmap bm;
    private TextView warnText;
    public String firstName;
    public String lastName;
    public String address;
    public String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstNameCtrl = (EditText) findViewById(R.id.firstName);
        lastNameCtrl = (EditText) findViewById(R.id.lastName);
        addressCtrl = (EditText) findViewById(R.id.addressText);
        imageButton = (ImageView) findViewById(R.id.imageButton1);
        warnText = (TextView) findViewById(R.id.textWarn);

        bm = BitmapFactory.decodeResource(getResources(), R.drawable.cam_button);

    }

    public void selectImage(View v){
        final CharSequence[] items = { "Use Camera", "Browse Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Use Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Browse Gallery")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                bm = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
//              String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File image = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    image.createNewFile();
                    fo = new FileOutputStream(image);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageButton.setImageBitmap(bm);
            } else if (requestCode == SELECT_FILE) {
                Uri imageURI = data.getData();
                String[] gallery = { MediaStore.MediaColumns.DATA };
                CursorLoader cursorLoader = new CursorLoader(this,imageURI, gallery, null, null,
                        null);
                Cursor cursor =cursorLoader.loadInBackground();
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String imagePath = cursor.getString(col_index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(imagePath, options);

                imageButton.setImageBitmap(bm);
            }
        }
    }
    public void onClickSignUp (View v){
        firstName = firstNameCtrl.getText().toString();
        lastName = lastNameCtrl.getText().toString();
        address = addressCtrl.getText().toString();

        boolean validationFlag = false;
        Bitmap checkImage = BitmapFactory.decodeResource(getResources(), R.drawable.cam_button);
        if (!firstName.isEmpty() && !lastName.isEmpty() && !address.isEmpty() && !bm.sameAs(checkImage)){
            validationFlag = true;
        }
        if (!validationFlag){
            warnText.setVisibility(View.VISIBLE);
        }
        else {
            Intent redirect = new Intent(MainActivity.this, MapsActivity.class);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 50, bs);
            redirect.putExtra("imageInfo", bs.toByteArray());
            //  redirect.putExtra("addressInfo", address);
            startActivity(redirect);
        }

    }
}
