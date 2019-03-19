#!/system/bin/sh
# https://github.com/termux/termux-api-package/tree/master/scripts

SCRIPTNAME=api-smali
show_usage () {
    echo "Usage: $SCRIPTNAME <application_name>"
    echo "Dump smali from specified app"
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


if [ $# = 1 ]; then
	/data/data/net.kwatts.android.droidcommandpro/files/bin/adbshellkit-api cmd_smali --es application_name "$1"
else
	echo "$SCRIPTNAME: No application name specified." >&2
	exit 1
fi

