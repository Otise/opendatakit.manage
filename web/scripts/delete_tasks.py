#!/usr/bin/python
import code
import getpass
import sys

#should be pythonpath
sys.path.append("~/google_appengine")
sys.path.append("~/google_appengine/lib/yaml/lib")

from google.appengine.ext.remote_api import remote_api_stub
from google.appengine.ext import db

def auth_func():
  return raw_input('Username:'), getpass.getpass('Password:')

if len(sys.argv) < 2:
  print "Usage: %s app_id [host]" % (sys.argv[0],)
app_id = sys.argv[1]
if len(sys.argv) > 2:
  host = sys.argv[2]
else:
  host = '%s.appspot.com' % app_id

remote_api_stub.ConfigureRemoteDatastore(app_id, '/remote_api', auth_func, host)

class Task(db.Model):
  pass

class Device(db.Model):
  pass

def delete_tasks():
  res = Task.all().fetch(1000)
  i = 1
  while(True):
    for t in res:
      t.delete()
      print 'task ' + str(i)
      i = i + 1
    
def delete_devices():
    res = Device.all().fetch(1000)
    i = 1
    for t in res:
      t.delete()
      print 'device ' + str(i)
      i = i + 1
     
delete_devices()
delete_tasks() 
