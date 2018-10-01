#!/system/bin/sh
########################
# DESCRIPTION:
#   Reboot Android into bootloader
#

setprop ctl.start pre-recovery
sleep 3
reboot recovery # fallback
