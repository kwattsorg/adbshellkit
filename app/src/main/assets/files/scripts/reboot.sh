#!/system/bin/sh
########################
# DESCRIPTION:
#   Reboot Android
#

setprop sys.powerctl reboot
sleep 3
reboot # fallback
