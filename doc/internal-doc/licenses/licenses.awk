#
# Generates the list of 3rd-party libs for use in the NOTICE.md file.
#
# Run with:
#     awk -f licenses.awk licenses.csv
#

BEGIN   { FS = ","; }
        { printf "%s\n\n  * License: %s\n  * Homepage: %s\n  * More information in folder: doc/3rd-party-licenses/%s\n\n", $1, $2, $3, $1; }
