import urllib
import urllib2

domain = 'http://odk-manage-stresstest.appspot.com'

def loadDevicesAndTasks(numDevices, numTasks, offset=0):
  for n in range(offset, offset+numDevices):
    imei = loadDevice(n)
    loadTasks(imei, numTasks)
    
def loadDevice(n):
  url = domain + '/register'
  imei = str(10000000000000000 + n)
  values = {'imei' : imei,
            'userid' : 'User' + str(n),
            'phonenumber' : '+123456' + str(n),
            'sim' : str(n),
            'imsi' : str(n) }
  
  doPost(url,values)
  print 'Loaded device ' + str(n)
  return imei
    
def loadTasks(imei, numTasks):
  for n in range (0, numTasks):
    url = domain + '/addTask'
    values = {'adminToken' : 'ureport',
              'imei' : imei,
              'type' : 'ADD_FORM',
              'name' : 'form' + str(n),
              'url' : 'http://www.example.com/form/' + str(n) }
    doPost(url,values)
    

    
def doPost(url, values):
  try:
    data = urllib.urlencode(values)
    req = urllib2.Request(url, data)
    urllib2.urlopen(req)
  except:
    print 'Error'

loadDevicesAndTasks(1, 0, offset=500)