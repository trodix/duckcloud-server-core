#!/bin/python3

import requests
import sys
import getopt
from faker import Faker
import uuid
from queue import Queue
from threading import Thread
import time

class Task:

    API_URL = "http://trodix.local:8010/api/v1/storage/nodes"
    fake = Faker()

    token_counter = 0
    token = ""
    ok_counter = 0
    fail_count = 0

    def refresh_token(self):

        headers = {}

        data = {
            'client_id': 'quack-duck-ecm-api',
            'client_secret': 'JJwFmrSUSPOHXF8ZbU8sABSBKe10V6XJ',
            'username': 'user2',
            'password': 'user2',
            'grant_type': 'password'
        }

        response = requests.post("http://localhost:8080/realms/market/protocol/openid-connect/token", headers=headers, data=data)

        token_ = response.json()['access_token']
        self.token_counter = self.token_counter + 1
        print("New token " + str(self.token_counter))

        return "Bearer " + token_

    def generate_data(self, i, thread_):
        print(f"iteration {i} from thread {thread_}")

        headers = {
            'Authorization': self.token
        }

        filename = f"{self.fake.file_name().rsplit('.', maxsplit=1)[0]}-{uuid.uuid4()}.pdf"

        data = {
            'type': 'cm:content',
            'parentId': '1',
            "properties['cm:name']": f"{filename}"
        }

        files = {
            'file': ('file.pdf', open('/media/DATA/workspace/DEV/_WKS_PDF_SIGN/duckcloud-server/utils/file.pdf', 'rb'), 'application/pdf', {'Expires': '0'})
        }

        response = requests.post(self.API_URL, headers=headers, data=data, files=files)

        # Vérifier la réponse
        if response.status_code == 200:
            print(f"OK. Created file: {filename}")
            self.ok_counter += 1
        elif response.status_code == 401:
            self.token = self.refresh_token()
            self.generate_data(i, thread_)
            self.fail_count += 1
            if self.fail_count > 1:
                print('Échec de la requête POST:', response.status_code)
        else:
            print('Échec de la requête POST:', response.status_code)

###################

def help():
    print ('create_data.py -n <number of files> -t <number of threads>')
    sys.exit()

def generate(limit, threads):
    start = time.perf_counter()
    print(f"Creating {limit} nodes with {threads} threads")
    t = Task()

    jobs = []

    for i in range(0, threads):
        for d in range(0, int(limit / threads)):
            thread_ = Thread(target=lambda: t.generate_data(d, i))
            jobs.append(thread_)

#     for d in range(0, limit):
#         t.generate_data(d, 1)

    # Start the threads (i.e. calculate the random number lists)
    for j in jobs:
        j.start()

    # Ensure all of the threads have finished
    for j in jobs:
        j.join()

    finish = time.perf_counter()
    print(f'Finished in {round(finish-start, 2)} second(s) with ok_counter = {t.ok_counter}')

def main(argv):
    opts, args = getopt.getopt(argv,"hn:t:",["filecount=", "threadscount"])
    LIMIT = 10
    THREADS = 1
    for opt, arg in opts:
        if opt == '-h':
            help()
        elif opt in ("-n", "--filecount"):
            LIMIT = int(arg)
        elif opt in ("-t", "--threadscount"):
            THREADS = int(arg)
    if len(opts) < 1:
        help()
    generate(LIMIT, THREADS)

if __name__ == "__main__":
    main(sys.argv[1:])
