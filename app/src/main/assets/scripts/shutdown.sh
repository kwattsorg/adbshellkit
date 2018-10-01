#!/system/bin/sh
########################
# DESCRIPTION:
#   Shutdown Android
#

setprop sys.powerctl shutdown
sleep 3
reboot -p # fallback
