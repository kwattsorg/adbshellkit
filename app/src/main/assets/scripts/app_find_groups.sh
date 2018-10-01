#!/usr/bin/env bash

# find / -group log -o -group inet -print
# find / -group input -o -group log -o -group adb -o -group sdcard_rw -o -group sdcard_r -o -group net_bt_admin -o -group net_bt -o -group inet -o -group net_bw_stats -o -group readproc -o -group uhid
find_files_with_group()
{
  find /  -path /proc -prune -o -group $i -print
}

USER_GROUPS='id -Gn | cut -d\) -f4'

for i in $USER_GROUPS
do
    find_files_with_group $i
done

