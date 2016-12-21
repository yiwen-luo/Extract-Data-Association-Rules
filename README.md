Project 3

# Extract-Data-Association-Rules
Extract Data Association Rules - COMS 6111 Project3

## Dataset
The "NYC Women's Resource Network Database" is chosen for this project, it can be downloaded here:
https://data.cityofnewyork.us/Social-Services/NYC-Women-s-Resource-Network-Database/pqg4-dm6b

This dataset provides information of resources for women in New York City. The reasons we choose such data set are as follows:

1. The size of the data set is suitable for the project. It has adequate rows and different attributes, which are suitable for the a-priori algorithm to run.

2. Data point under most data attributes, i.e. data cell, are "Y/N" boolean indicators. Useful association rules can be easily derived by simply verify the boolean value of the current cell. 

Data pre-processing has been done on the dataset. Non-boolean columns with their attributes are removed since they provide irrelevant information of the association. In detail, “Organization Name", "Fax", "Phone", "URL", "noURL", "Description", "outsideLoc", "outsideLocDesc", "Women's Groups", "Comments", "Published", "Location1" and "Location2" columns are removed. Save the file and good to go!

## Build and Run
`mvn clean install`

`mvn package`

`./run.sh <DATA_SET.csv> <MIN_SUPPORT> <MIN_CONFIDENCE>`

Sample Run:

`./run.sh INTEGRATED-DATASET.csv 0.15 0.7`

## Sample Run and Interesting Findings
The sample run sets the confidence threshold to 70% and support threshold to 15% after many tests. From the following example:

[Manhattan] => [Bronx](Conf: 72%, Supp: 16%)
[Queens] => [Bronx](Conf: 70%, Supp: 15%)

we can learn that at the edge of the threshold. The result is still meaningful but if we adjust the threshold, irrelevant information will be returned. In this example, Manhattan and Bronx are adjacent boroughs. Also, Queens and Bronx are adjacent boroughs. It is more likely for an organization to operate in two adjacent boroughs. In contrast, something like [Staten Island] => [Bronx] is not with high confidence very rare organizations will only operate in these two boroughs. This reflects the real situation.

There are some interesting findings:
If an organization is operating in Staten Island, it is highly likely to operate in all five boroughs.
If an organization is working on education and operating non-profit, it is likely to be in Manhattan.

