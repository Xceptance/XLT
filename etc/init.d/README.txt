1. Put the start script to /etc/init.d/xlt

2. Set permissions
   chmod 755 /etc/init.d/xlt

3. If you want to start/stop XLT agent controller on machine boot/shutdown automatically try
   update-rc.d xlt defaults 20 20
   (adapt start/stop settings and priority to your needs)

4. Adapt the variables XLT_USER, XLT_PATH, and LOG_FILE.
   Adapt the info section as well. For details see: https://wiki.debian.org/LSBInitScripts

5. Try it
   /etc/init.d/xlt start