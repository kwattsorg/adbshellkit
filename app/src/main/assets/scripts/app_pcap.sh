#!/system/bin/sh
app_package="net.kwatts.android.droidcommandpro"
auth_token=""

# [0-9]{3}[A-Z]{3} // 3 digits, then 3 capital letters
for i in "$@"
do
  case $i in
    --app)
	  app_package="${i#*=}"
	  shift
	  ;;
    --auth_token)
	  auth_token="${i#*=}"
	  shift
	  ;;
	*)
	  # unknown option
  esac
done
curl --request GET --url 'https://api.appthority.com/api/v3/apps/search?application_name={{test_app_name}}&auth_token=$auth_token'
curl --request GET --url 'https://api.appthority.com/api/v3/apps/{{test_app_id}}/pcap?auth_token=$auth_token'

