import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error

# Load the data from the CSV file
data = pd.read_csv('fingerprint_data4.csv')

# Display the first few rows of the dataframe to understand the structure
print(data.head())

# Preprocess the data: Extract features (RSSI values) and labels (coordinates)
# Drop columns that are not needed for training
features = data.drop(columns=['id', 'name', 'x', 'y'])
labels = data[['x', 'y']]

# Split the data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(features, labels, test_size=0.4, random_state=42)

# Train a machine learning model
model = RandomForestRegressor(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# Predict coordinates on the test set
y_pred = model.predict(X_test)

# Evaluate the model
mse = mean_squared_error(y_test, y_pred)
print(f"Mean Squared Error: {mse}")

# accuracy
accuracy = model.score(X_test, y_test)
print(f"Accuracy: {accuracy}")

# Print a sample of the predictions
sample_predictions = pd.DataFrame(y_pred, columns=['pred_x', 'pred_y'])
print(sample_predictions.head())

# Print a sample of the predictions along with actual values
sample_predictions = pd.DataFrame(y_pred, columns=['pred_x', 'pred_y'])
sample_actuals = y_test.reset_index(drop=True)
sample_results = pd.concat([sample_actuals, sample_predictions], axis=1)

print(sample_results)

import pickle

# Assuming 'model' is your trained RandomForestRegressor
with open('saved_model.pkl', 'wb') as f:
    pickle.dump(model, f)
