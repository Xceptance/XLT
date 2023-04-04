#
# Generates the list of 3rd-party libs for use in the NOTICE.md file.
#
# Run with:
#     awk -f licenses.awk licenses.csv
#

BEGIN   { 
            FS = ","; 
        }
        { 
            printf "%s\n\n", $1;
            printf "  * License: %s\n", $2;
            printf "  * Homepage: %s\n", $3;
            if ($5 != "") printf "  * %s\n", $5;
            printf "  * More information in folder: doc/3rd-party-licenses/%s\n\n", $1;
        }
