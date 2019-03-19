#!/system/bin/sh

SCRIPTNAME=api-cmd_run_system
show_usage () {
    echo "Usage: $SCRIPTNAME <system cmd>"
    echo "Runs system command returning the stdout"
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
	/data/data/net.kwatts.android.droidcommandpro/files/bin/adbshellkit-api cmd_run_system --es exec_command "$1"
else
	echo "$SCRIPTNAME: No system command specified." >&2
	exit 1
fi
