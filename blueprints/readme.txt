You need to run the following command as a super user or in the command prompt opened with administrative privileges to setup the plugin that is needed for the UI piece. (http://stackoverflow.com/questions/15278215/maven-project-with-javafx-with-jar-file-in-lib)

“mvn com.zenjava:javafx-maven-plugin:2.0:fix-classpath”

After running the above command you can run “mvn -DskipTests=true -Dmaven.javadoc.skip=true -U install”
