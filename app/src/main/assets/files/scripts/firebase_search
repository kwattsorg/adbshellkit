#!/system/bin/sh

# find /data/data/com.google.android.gms -type f  -exec egrep -Ha firebaseio {} \;
# strings /data/data/com.google.android.gms/files/AppDataSearch/main/cur/* | grep firebase
find /data/data -name com.google.firebase.auth.* -exec egrep -oH 'access_token&quot;:&quot;.+expires_in' {} \; > /data/local/tmp/firebase.txt

for i in `cat /data/local/tmp/firebase.txt`
do
  echo "APP="
  echo $i | cut -d':' -f1 | cut -d '/' -f4
  echo "AUTH="
  echo $i | cut -d':' -f3 | cut -d';' -f2 | cut -d'&' -f1
done
