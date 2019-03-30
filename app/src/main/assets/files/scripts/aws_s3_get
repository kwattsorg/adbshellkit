#!/bin/sh
# This works for cross region
# https://docs.aws.amazon.com/AmazonS3/latest/API/v2-RESTBucketGET.html to list keys 
outputFile="/PATH/TO/FILE"
awsFile="BUCKETPATH/TO/FILE"
bucket="SOME-BUCKET"
resource="/${bucket}/${awsFile}"
contentType="application/x-compressed-tar"
# Change the content type as desired
dateValue=`TZ=GMT date -R`
#Use dateValue=`date -R` if your TZ is already GMT
stringToSign="GET\n\n${contentType}\n${dateValue}\n${resource}"
s3Key="ACCESS_KEY_ID"
s3Secret="SECRET_ACCESS_KEY"
signature=`echo -n ${stringToSign} | openssl sha1 -hmac ${s3Secret} -binary | base64`
curl -H "Host: ${bucket}.s3.amazonaws.com" \
     -H "Date: ${dateValue}" \
     -H "Content-Type: ${contentType}" \
     -H "Authorization: AWS ${s3Key}:${signature}" \
     https://${bucket}.s3.amazonaws.com/${awsFile} -o $outputFile
