package com.example.myapplication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class TensorFlowLiteModel {
    private Interpreter interpreter;
    private static final String TAG = "TensorFlowLiteModel";

    public TensorFlowLiteModel(AssetManager assetManager, String modelPath) {
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            interpreter = new Interpreter(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Error reading model", e);
        }
    }

    public static int getIndexOfMaxValue(float[] array) {
        Log.d("output", "" + Arrays.toString(array));
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty.");
        }

        int maxIndex = 0;
        float maxValue = array[0];

        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    public int predict(float[] input) {
        float[][] output = new float[1][3];  // Adjust size based on your output layer
        interpreter.run(input, output);
        Log.d(TAG, "Output: " + arrayToString(output[0]));
//        return output[0];
        return getIndexOfMaxValue(output[0]);
    }

    private String arrayToString(float[] array) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i < array.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
