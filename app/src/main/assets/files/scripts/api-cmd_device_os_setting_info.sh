#!/system/bin/sh


SCRIPTNAME=api-cmd_device_os_setting_info
show_usage () {
    echo "Usage: $SCRIPTNAME"
    echo "Dumps device os setting info"
    exit 0
}

while getopts :h option
do
    case "$option" in
        h) show_usage;;
        ?) echo "$SCRIPTNAME: illegal option -$OPTARG"; exit 1;
    esac
done
shift $(($OPTIND-1))

if [ $# != 0 ]; then echo "$SCRIPTNAME: too many arguments"; exit 1; fi

/data/data/net.kwatts.android.droidcommandpro/files/bin/adbshellkit-api cmd_device_os_setting_info

