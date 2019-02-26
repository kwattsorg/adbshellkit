#!/system/bin/sh
########################
# DESCRIPTION:
#   Runs command under package UID
#
# USAGE:
#   app_run_as [--app=PACKAGE] [--command=STRING]
#
app_package="net.kwatts.android.droidcommandpro"
command="id"

# [0-9]{3}[A-Z]{3} // 3 digits, then 3 capital letters
for i in "$@"
do
  case $i in
    --app)
	  app_package="${i#*=}"
	  shift
	  ;;
    --command)
	  command="${i#*=}"
	  shift
	  ;;
	*)
	  # unknown option
  esac
done
# android.permission.INTERACT_ACROSS_USERS_FULL
# su u0_a173 -c am start -a android.intent.action.VIEW -d chrome://view-http-cache/
# adb backup -f ~/data.ab -noapk com.sfmta.mt.mobiletickets
# sailfish:/ # setprop security.perf_harden 0
# sailfish:/ # cat /proc/sys/kernel/perf_event_paranoid

APP_UID=$( ps -e | grep $app_package | cut -d' ' -f1 )
su $APP_UID -c ${command}
