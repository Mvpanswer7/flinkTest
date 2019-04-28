#!/usr/bin/env bash
echo "--------------------pre_build.sh--------------------"
echo "-----------------------Start------------------------"

case ${deploy_env} in 
    deploy)
	THIS_DIR=`dirname $(readlink -f $0)`
	COMMIT_ID=`git rev-parse HEAD|cut -b 1-8`
	if [ $TAGversion ]; then
	    TAG=$TAGversion
	    LATEST="1"
	    sed -i '/flinkVersion/s/flinkVersion/'${TAG}'/g' $THIS_DIR/../pom.xml
	else
	    TAG=${COMMIT_ID}
	    sed -i '/flinkVersion/s/flinkVersion/'${TAG}'-SNAPSHOT/g' $THIS_DIR/../pom.xml
	fi
	;;
    rollback)
        echo "rollback: $deploy_env"
        echo "version: $version"
        cp -rf ${JENKINS_HOME}/jobs/$JOB_NAME/builds/${version}/archive/** .
        
        tag=`cat $WORKSPACE/CONTAINER_TAG`
        echo ${tag}
        echo "success"
        ;;
    *)
    exit
        ;;
esac
echo "--------------------pre_build.sh--------------------"
echo "----------------------All done----------------------"
