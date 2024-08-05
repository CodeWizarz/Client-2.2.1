##############################################################################
# $Revision: 72025 $
# $Author: warangkana.yoomieng $
# $Date: 2019-05-09 16:32:55 +0700 (Thu, 09 May 2019) $
# $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/resource/start_snapshot_auto_delete.sh $
# $Id: start_snapshot_auto_delete.sh 72025 2019-05-09 09:32:55Z warangkana.yoomieng $
##############################################################################
# This script needed to be exist instead of execute 'build.py' direcly, due to we had customer deployed appliance by converted VMware to HyperV.
# And we foudn that Java environment variable was not able to set properly by neither of put the export variable in
# '~/.bashrc', '~/.bash_profile', '/etc/profile',  '/etc/bashrc' and new script in '/etc/profile.d/'.
# Hence, just make script to set environemnt before execute command to start client.

source /opt/res/homes/res/scripts/set_java_envs.sh
python /opt/res/rapidsnapshot/start_snapshot_auto_delete.py a=$1 b=$2 c=$3 d=$4
