#!/system/bin/sh
########################
# DESCRIPTION:
#   Dumps databases from a specified directory
#
# USAGE:
#   app_database [--app=package_name]

base_dir="/data/data"
app_package="net.kwatts.android.droidcommandpro"

for i in "$@"
do
  case $i in
    --path)
	  path="${i#*=}"
	  shift
	  ;;
    --app_package)
	  app_package="${i#*=}"
	  shift
	  ;;
	*)
	  # unknown option
  esac
done

PLATFORM=$(getprop ro.product.cpu.abi)
case "$PLATFORM" in
  x86|x86_64)
    SQLITE3="/system/xbin/sqlite3"
    ;;
  *)
    SQLITE3="/data/data/net.kwatts.android.droidcommandpro/cache/bin/sqlite3.armv7"
    ;;
esac

find ${base_dir}/${app_package}/databases -type f ! -name '*journal' \
 -exec echo "--- BEGIN DB FILE ---" \; \
 -exec echo {} \; \
 -exec $SQLITE3 {} .schema \; \
 -exec $SQLITE3 {} .dump \; \
 -exec echo "--- DONE DB FILE ---" \;

