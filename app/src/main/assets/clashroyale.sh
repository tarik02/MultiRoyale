#!/system/bin/sh
PROFILE_NAME="$1"

PACKAGE="com.supercell.clashroyale"
CURRENT_PROFILE_DIR="/data/data/$PACKAGE"

PROFILES_DIR="/data/data/$PACKAGE/profiles"
PROFILE_DIR="$PROFILES_DIR/$PROFILE_NAME"

APP_LS=$(su -c ls -ld "$CURRENT_PROFILE_DIR")
APP_USER=$(echo $APP_LS | cut -d' ' -f3)
APP_GROUP=$(echo $APP_LS | cut -d' ' -f4)

run() {
	echo "\$ $@"
	"$@"
}

if [ "$APP_USER" != "$USER" ]; then
	# We are in root
	run am kill "$PACKAGE"
	run am force-stop "$PACKAGE"

	SCRIPT="$(cd "$(dirname "$0")"; pwd -P)/$(basename "$0")"
	TEMP=`mktemp`

	run cp "$SCRIPT" "$TEMP"
	run chown "$APP_USER:$APP_GROUP" $TEMP
	run su "$APP_USER" -c "/system/bin/sh $TEMP $@"
	run rm "$TEMP"
	
	INTENT=$(cmd package resolve-activity --brief -c android.intent.category.LAUNCHER $PACKAGE | busybox tail -1)
	run am start -n "$INTENT"
	exit 0
fi

if [ -f "$PROFILES_DIR/current" ]; then
	OLD_PROFILE=$(cat "$PROFILES_DIR/current")
else
	OLD_PROFILE=""
fi

if [ "$OLD_PROFILE" = "$PROFILE_NAME" ]; then
	echo "There is no need to change profile"
else
	if [[ ! -z "$OLD_PROFILE" ]]; then
		OLD_PROFILE_DIR="$PROFILES_DIR/$OLD_PROFILE"
		echo "Saving old profile..."
		run rm -rf "$OLD_PROFILE_DIR"
		run mkdir -p "$OLD_PROFILE_DIR"
		#run mkdir -p "$CURRENT_PROFILE_DIR"
		run mv "$CURRENT_PROFILE_DIR/shared_prefs" "$OLD_PROFILE_DIR/shared_prefs"
	fi

	echo "Checking the profile..."
	if [ -d "$PROFILE_DIR" ]; then
		echo "Applying the profile..."

		#run mkdir -p "$PROFILE_DIR/shared_prefs"
		run rm -rf "$CURRENT_PROFILE_DIR/shared_prefs"
		run mv "$PROFILE_DIR/shared_prefs" "$CURRENT_PROFILE_DIR/shared_prefs"
	else
		echo "Creating the profile"

		#run mkdir -p "$CURRENT_PROFILE_DIR/shared_prefs"
		#run mkdir -p "$PROFILE_DIR/shared_prefs"
		run mkdir -p "$PROFILE_DIR"
	fi

	echo "Writing current file"
	echo -n "$PROFILE_NAME" > "$PROFILES_DIR/current"
fi

