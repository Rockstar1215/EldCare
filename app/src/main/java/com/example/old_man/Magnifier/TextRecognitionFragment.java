package com.example.old_man.Magnifier;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.old_man.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;

public class TextRecognitionFragment extends Fragment implements OnInitListener {

    private SurfaceView cameraView;
    private EditText mScannedText;
    private CameraSource cameraSource;
    private Button mCopyButton;
    private Button mOpenImageBtn;
    private Button mOpenCameraBtn;
    private ImageView mImageView;
    private Button mShareBtn;
    private Button mConvertToPDFBtn;

    private InterstitialAd mImageBtnAd;
    private InterstitialAd mCameraBtnAd;
    private InterstitialAd mPDFBtnAd;

    private int IMAGE_RESULT_CODE = 534;
    private TextToSpeech textToSpeech;
    private Button mStartScanningBtn;
    private Button mStopScanningBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text_recognition, container, false);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(requireActivity().getApplicationContext(), this);

        cameraView = v.findViewById(R.id.surface_view);
        mScannedText = v.findViewById(R.id.scanned_text);
        mCopyButton = v.findViewById(R.id.copy_btn);
        mOpenImageBtn = v.findViewById(R.id.open_image_btn);
        mOpenCameraBtn = v.findViewById(R.id.open_camera_btn);
        mImageView = v.findViewById(R.id.image);
        mShareBtn = v.findViewById(R.id.share_btn);
        mConvertToPDFBtn = v.findViewById(R.id.convert_to_pdf_btn);

        mImageBtnAd = new InterstitialAd(getActivity());
        mCameraBtnAd = new InterstitialAd(getActivity());
        mPDFBtnAd = new InterstitialAd(getActivity());

        mImageBtnAd.setAdUnitId(getString(R.string.text_recognition_imagebtn_ad));
        mCameraBtnAd.setAdUnitId(getString(R.string.text_recognition_camerabtn_ad));
        mPDFBtnAd.setAdUnitId(getString(R.string.text_recognition_pdfbtn_ad));

        mImageBtnAd.loadAd(new AdRequest.Builder().build());
        mCameraBtnAd.loadAd(new AdRequest.Builder().build());
        mPDFBtnAd.loadAd(new AdRequest.Builder().build());

        TextRecognizer recognizer = new TextRecognizer.Builder(requireActivity().getApplicationContext()).build();
        if (!recognizer.isOperational()) {
            Log.w("Recognizer Fragment", "Detector dependencies are not available");
        } else {
            cameraSource = new CameraSource.Builder(getActivity().getApplicationContext(), recognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            recognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        mScannedText.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                String scannedText = stringBuilder.toString();
                                mScannedText.setText(scannedText);

//                                // Read out the scanned text
//                                speakOut(scannedText);
                            }
                        });
                    }
                }
            });
        }

        mOpenCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraBtnAd.isLoaded()) {
                    mCameraBtnAd.show();
                }
                mOpenImageBtn.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.GONE);
                cameraView.setVisibility(View.VISIBLE);
                mOpenCameraBtn.setVisibility(View.GONE);
            }
        });

        mCopyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("QR text", mScannedText.getText().toString());
                assert clipboardManager != null;
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getActivity(), "Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        mOpenImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageBtnAd.isLoaded()) {
                    mImageBtnAd.show();
                }
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, IMAGE_RESULT_CODE);
            }
        });

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(mScannedText.getText().toString());
            }

            private void share(String toString) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Scanned Text");
                shareIntent.putExtra(Intent.EXTRA_TEXT, toString);
                startActivity(Intent.createChooser(shareIntent, "Share Using"));
            }
        });

        mConvertToPDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPDFBtnAd.isLoaded()) {
                    mPDFBtnAd.show();
                }
                createPDF(mScannedText.getText().toString());
            }

            private void createPDF(String text) {
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();

                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                canvas.drawText(text, 10, 50, paint);
                document.finishPage(page);

                try {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Magnifier";
                    File dir = new File(path);
                    if (!dir.exists())
                        dir.mkdirs();
                    File file = new File(dir, UUID.randomUUID().toString() + ".pdf");
                    OutputStream fout = new FileOutputStream(file);
                    document.writeTo(fout);
                    Toast.makeText(getActivity(), "Created", Toast.LENGTH_SHORT).show();
                    speakOut(text); // Read out the content after creating PDF
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "File Not Found", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Couldn't create PDF", Toast.LENGTH_SHORT).show();
                } finally {
                    document.close();
                }
            }
        });
        mStartScanningBtn = v.findViewById(R.id.start_scanning_btn);
        mStopScanningBtn = v.findViewById(R.id.stop_scanning_btn);

        mStartScanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });

        mStopScanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanning();
            }
        });
        return v;
    }

    private void startScanning() {
        // Add logic to start the scanning process
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start(cameraView.getHolder());
                mStartScanningBtn.setVisibility(View.GONE);
                mStopScanningBtn.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopScanning() {
        // Add logic to stop the scanning process
        if (cameraSource != null) {
            cameraSource.stop();
            mStartScanningBtn.setVisibility(View.VISIBLE);
            mStopScanningBtn.setVisibility(View.GONE);
            String scannedText = mScannedText.getText().toString();

            // Read out the scanned text
            speakOut(scannedText);
        }
    }


    public void surfaceCreated(SurfaceHolder holder) {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startScanning(); // Start scanning when the surface is created
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        stopScanning(); // Stop scanning when the surface is destroyed
    }
    private void speakOut(String text) {
        if (textToSpeech != null && text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language not supported");
            }
        } else {
            Log.e("TextToSpeech", "Initialization failed");
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                mImageView.setImageBitmap(selectedImage);
                cameraView.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mOpenImageBtn.setVisibility(View.GONE);
                mOpenCameraBtn.setVisibility(View.VISIBLE);
                Frame frame = new Frame.Builder().setBitmap(selectedImage).build();
                TextRecognizer recognizer = new TextRecognizer.Builder(getActivity()).build();
                SparseArray<TextBlock> items = recognizer.detect(frame);

                if (items.size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < items.size(); i++) {
                        stringBuilder.append(items.valueAt(i).getValue());
                        stringBuilder.append("\n");
                    }
                    String scannedText = stringBuilder.toString();
                    mScannedText.setText(scannedText);
                    speakOut(scannedText); // Read out the scanned text
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "No Image Picked", Toast.LENGTH_SHORT).show();
        }
    }
}
