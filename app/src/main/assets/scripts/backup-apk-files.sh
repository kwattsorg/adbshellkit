#!/system/bin/sh
########################
# DESCRIPTION:
#   Backup installed APK files
#
# USAGE:
#   backup-apk-file.sh [--ignore-system-apps] [--destination=PATH]
#

backup_system_apps=true
backup_location=$EXTERNAL_STORAGE/apk_files

for i in "$@"
do
  case $i in
    --ignore-system-apps)
	  backup_system_apps=false
	  shift
	  ;;
	-d|--destination)
	  backup_location="${i#*=}"
	  shift
	  ;;
	*)
	  # unknown option
  esac
done

# create the backup directory if it doesn't exist
if [ ! -d "${backup_location}" ]
then
  mkdir -p "${backup_location}"
fi

# loop through all installed applications
for line in $(pm list packages -f)
do
  apk_file=$(echo $line | cut -d: -f2 | cut -d= -f1)
  if ! $backup_system_apps
  then
    if [[ "${apk_file}" == "/system/"* ]]
	then
	  continue # skip system apk
	fi
  fi
  package_name=$(echo $line | cut -d= -f2)
  echo $package_name
  cp "${apk_file}" "${backup_location}/${package_name}.apk"
done

