package com.example.old_man.Magnifier;

import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.old_man.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class QRScannerFragment extends Fragment {

    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Size imageDimension;
    private static final int REQUEST_CAMERA_PERMISSION = 123;

    private TextureView mTextureView;
    private TextView mScannedText;
    private Button mCopyBtn;
    private Button mStartBtn;
    private Button mShareBtn;
    private QRCodeReader qrCodeReader;
    private TextView mHintText;

    private InterstitialAd mStartBtnAd;
    private InterstitialAd mCopyBtnAd;
    private Handler handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_q_r_scanner, container, false);
        mTextureView = v.findViewById(R.id.texture_view);
        mScannedText = v.findViewById(R.id.scanned_text);
        mStartBtn = v.findViewById(R.id.start_btn);
        mCopyBtn = v.findViewById(R.id.copy_btn);
        mShareBtn = v.findViewById(R.id.share_btn);
        mHintText = v.findViewById(R.id.hint_text);

        qrCodeReader = new QRCodeReader();

        mStartBtnAd = new InterstitialAd(getActivity());
        mCopyBtnAd = new InterstitialAd(getActivity());
        mStartBtnAd.setAdUnitId(getString(R.string.qr_fragment_start_btn_ad));
        mCopyBtnAd.setAdUnitId(getString(R.string.qr_fragment_copy_btn_ad));
        mStartBtnAd.loadAd(new AdRequest.Builder().build());
        mCopyBtnAd.loadAd(new AdRequest.Builder().build());

        assert mTextureView != null;
        mTextureView.setSurfaceTextureListener(textureListener);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                try {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics("0");
                    StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);

                    List<Size> outputSizes = Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888));
                    Size largest = Collections.max(outputSizes, new CompareSizesByArea());

                    final ImageReader reader = ImageReader.newInstance(largest.getWidth() / 16, largest.getHeight() / 16, ImageFormat.YUV_420_888, 2);
                    reader.setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler);

                    CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(new Surface(mTextureView.getSurfaceTexture()));
                                builder.addTarget(reader.getSurface());
                                session.setRepeatingRequest(builder.build(), null, null);
                                mStartBtn.setVisibility(View.GONE);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    };
                    List<Surface> surfaces = new ArrayList<>();
                    surfaces.add(new Surface(mTextureView.getSurfaceTexture()));
                    surfaces.add(reader.getSurface());
                    cameraDevice.createCaptureSession(surfaces, stateCallback, mBackgroundHandler);

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                mStartBtn.setVisibility(View.GONE);
                mShareBtn.setVisibility(View.VISIBLE);

            }
        });

        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(mScannedText.getText().toString());
            }
        });

        mCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCopyBtnAd.isLoaded())
                    mCopyBtnAd.show();
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("QR text",mScannedText.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getActivity(), "Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle b = msg.getData();
                boolean hide = b.getBoolean("hideHint");
                if(hide){
                    mHintText.setVisibility(View.GONE);
                    mScannedText.setVisibility(View.VISIBLE);
                }
                mScannedText.setText(b.getString("text"));
            }
        };

        return v;
    }

    private void share(String toShare) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Scanned Text");
        shareIntent.putExtra(Intent.EXTRA_TEXT, toShare);
        startActivity(Intent.createChooser(shareIntent, "Share Using"));
    }

    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = null;
            img = reader.acquireLatestImage();
            Result rawResult = null;

            try {
                if (img != null) {
                    ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    int width = img.getWidth();
                    int height = img.getHeight();
                    PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    rawResult = qrCodeReader.decode(bitmap);
                    Log.d("Code: ", rawResult.getText());


                    Message message = Message.obtain();
                    Bundle b = new Bundle();
                    if(mHintText.getVisibility() == View.VISIBLE){
                        b.putBoolean("hideHint",true);
                    }
                    else
                        b.putBoolean("hideHint",false);
                    b.putString("text",rawResult.getText());
                    message.setData(b);
                    handler.sendMessage(message);
                }
            } catch (NullPointerException | ReaderException e) {
                e.printStackTrace();
            } finally {
                qrCodeReader.reset();
                if (img != null)
                    img.close();
            }
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(getActivity(), "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
            Log.d("Texture","Texture available");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}