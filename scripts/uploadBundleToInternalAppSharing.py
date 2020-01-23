#!/usr/bin/env python3
#
# Copyright 2014 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the 'License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#Refs: https://github.com/googlesamples/android-play-publisher-api/blob/master/v3/python/basic_upload_apks.py
#Doc: https://docs.google.com/document/d/1tNitzRi6jDGCGW88SxYZquGBRzCrbs8wS6vGaCYATKA/edit

"""Uploads an Android App Bundle to the internal app sharing."""

import argparse
from googleapiclient.discovery import build
import httplib2
from oauth2client import client
from oauth2client.service_account import ServiceAccountCredentials
import os

argparser = argparse.ArgumentParser(add_help=False)
argparser.add_argument('service_account',
                       help="Service account Email Id used to upload APK/Bundle to Google Play")
argparser.add_argument('aab_file',
                       help='The path to the .aab file to upload.')

def main():
    # Process flags and read their values.
    flags = argparser.parse_args()
    p12_key_file_path = os.path.abspath(os.path.dirname(__file__)) + '/api-57wnkjfwhffho-14609-34t43r343.p12'

    # Authenticate and construct service.
    # Create an httplib2.Http object to handle our HTTP requests and authorize it
    # with the Credentials. Note that the first parameter, service_account_name,
    # is the Email address created for the Service account. It must be the email
    # address associated with the key that was created.
    credentials = ServiceAccountCredentials.from_p12_keyfile(
        flags.service_account,
        p12_key_file_path,
        scopes=['https://www.googleapis.com/auth/androidpublisher'])
    http = httplib2.Http()
    http = credentials.authorize(http)

    service = build('androidpublisher', 'v3', http=http)

    package_name = 'com.test.dynamictest'
    aab_file = flags.aab_file

    try:
        aab_response = service.internalappsharingartifacts().uploadbundle(
            packageName=package_name,
            media_body=aab_file,
            media_mime_type="application/octet-stream"
        ).execute()

        # print ('Successfully uploaded AAB')
        print (aab_response['downloadUrl'])

    except client.AccessTokenRefreshError:
        print ('The credentials have been revoked or expired, please re-run the '
               'application to re-authorize')

if __name__ == '__main__':
    main()
