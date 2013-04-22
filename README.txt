Honor's Project for Carleton University. This android application will hook up to your facebook account, and recommend you a movie based on what you and your friends like. Uses Rotten Tomatoes and OMDB as webservices to accomplish it's task.

This project demonstrates working with Facebook and various Webservices to create an application which can be useful to a user. The application runs on android and recommends the user movies, flagging the ones that have been identified as favorite movies of friends who are similar enough to the user.

User similarity is determined by Pearson coefficients. The Pearson Threshold for similarity can be configured within the application's settings.

The application saves movies to JSON files in order to cache them. It can also save recommendations to csv files so that the data can be analyzed in a spreadsheet program.