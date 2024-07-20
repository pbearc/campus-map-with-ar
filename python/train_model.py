# import asyncio
# from bleak import BleakScanner
# import pandas as pd
# from sklearn.ensemble import RandomForestClassifier
# from sklearn.model_selection import train_test_split
# from sklearn.metrics import accuracy_score, classification_report
# from beacons import target_beacons_name
# import joblib

# input_data = [-100]*len(target_beacons_name)  # List to store scanned data

# async def main():
#     # Load data from CSV for training
#     data = pd.read_csv('output.csv')
    
#     # Ensure target beacons are part of the data columns
#     target_columns = [col for col in target_beacons_name if col in data.columns]
    
#     # Separate features (X) and target (y)
#     X = data[target_columns]  # Using RSSI values as features
#     y = data['Point']  # Assuming 'Point' is the target label to predict
    
#     # Split data into training and testing sets
#     X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
#     # Initialize the Random Forest Classifier
#     clf = RandomForestClassifier(n_estimators=100, random_state=42)
    
#     # Train the classifier
#     clf.fit(X_train, y_train)
    
#     # Predict on the test set
#     y_pred = clf.predict(X_test)
    
#     # Evaluate model performance
#     accuracy = accuracy_score(y_test, y_pred)
#     print(f"Accuracy: {accuracy:.2f}")
    
#     # Print classification report
#     print(classification_report(y_test, y_pred))
    
#     # Save the RandomForestClassifier model
#     joblib.dump(clf, 'trained_model_rf.pkl')

# if __name__ == "__main__":
#     asyncio.run(main())

import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from sklearn.model_selection import train_test_split
import numpy as np
from beacons import target_beacons_name

# Example dimensions
input_dim = len(target_beacons_name)  # Number of input features (target beacons)
num_classes = 2  # Number of classes (replace with your actual number of classes)

# Generate example data (replace with your actual data loading)
X = np.random.rand(100, input_dim)  # Example features
y = np.random.randint(num_classes, size=100)  # Example labels

# Split data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Define the model architecture
model = Sequential([
    Dense(64, activation='relu', input_dim=input_dim),  # Input layer with 64 neurons
    Dense(32, activation='relu'),  # Hidden layer with 32 neurons
    Dense(num_classes, activation='softmax')  # Output layer with softmax activation for multi-class classification
])

# Compile the model
model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',  # Use sparse categorical crossentropy for integer labels
              metrics=['accuracy'])

# Train the model
model.fit(X_train, y_train, epochs=10, batch_size=32, validation_data=(X_test, y_test))

# Evaluate the model on test data
loss, accuracy = model.evaluate(X_test, y_test)
print(f"Test Loss: {loss:.4f}")
print(f"Test Accuracy: {accuracy:.4f}")

model.save('trained_model.h5')


# # Convert the model to TensorFlow Lite
# converter = tf.lite.TFLiteConverter.from_saved_model('trained_model_tf')
# tflite_model = converter.convert()

# # Save the TensorFlow Lite model
# with open('model.tflite', 'wb') as f:
#     f.write(tflite_model)
