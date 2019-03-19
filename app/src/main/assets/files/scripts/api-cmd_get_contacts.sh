#!/system/bin/sh


SCRIPTNAME=api-cmd_get_contacts
show_usage () {
    echo "Usage: $SCRIPTNAME"
    echo "Dumps contacts on device"
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

/data/data/net.kwatts.android.droidcommandpro/files/bin/adbshellkit-api cmd_get_contacts

