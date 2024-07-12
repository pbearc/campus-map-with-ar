use testrssi to collect data at each RP. store data for each corridor in corridor_.py
combine all data in whole_floor.py.
use trainmodel.py to train model and save trained model at saved_model.pk.
ps: trained model as changed from randomforest to knn model to test for prediction accuracy, though no obvious difference is noticed.

collectfortest.py collects rssi value of user's standing point and store at collectedfortest.csv
userentrypoint.py loads trained model from saved_model.pkl to predict user's coordinate.

ps: will change from predict coordinate to predict point.