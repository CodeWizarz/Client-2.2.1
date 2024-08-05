java -cp config:classes:lib/* -Xmx64m -Dlog4j.configuration=log4j.properties -Djppf.config=jppf.properties \
-Djava.util.logging.config.file=config/logging.properties org.jppf.application.template.TemplateApplicationRunner \
/mnt/scratch/JPPF/oratest47.BUILD1.3.3.bwe \
/mnt/scratch/JPPF/simple \
/mnt/scratch/JPPF/rapidclient/build.py \
true
