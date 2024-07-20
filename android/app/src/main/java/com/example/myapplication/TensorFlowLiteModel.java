package com.example.myapplication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class TensorFlowLiteModel {

    private static final String TAG = "TensorFlowLiteModel";
    private Interpreter interpreter;
    private int inputDimension;

    public TensorFlowLiteModel(Context context) {
        try {
            interpreter = new Interpreter(loadModelFile(context));
            inputDimension = 1;
        } catch (IOException e) {
            Log.e(TAG, "Error loading TensorFlow Lite model: " + e.getMessage());
        }
    }

    private ByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    int getInputDimension(){
        return inputDimension;
    }

    public int predictPoint(float[] rssiValues) {
        if (interpreter == null) {
            Log.e(TAG, "TensorFlow Lite interpreter is not initialized.");
            return -1; // Or handle the error appropriately
        }

        try {
            // Prepare input buffer
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(rssiValues.length * 4);
            inputBuffer.order(ByteOrder.nativeOrder());
            for (float value : rssiValues) {
                inputBuffer.putFloat(value);
            }
            inputBuffer.rewind();

            // Prepare output buffer
            int[] output = new int[1]; // Assuming single output
            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(output.length * 4);
            outputBuffer.order(ByteOrder.nativeOrder());
            outputBuffer.rewind();

            // Run inference
            interpreter.run(inputBuffer, outputBuffer);

            // Process the output
            outputBuffer.rewind();
            outputBuffer.asIntBuffer().get(output);

            // Return predicted point
            return output[0];

        } catch (Exception e) {
            Log.e(TAG, "Error running TensorFlow Lite inference: " + e.getMessage());
            return -1; // Or handle the error appropriately
        }
    }
}
