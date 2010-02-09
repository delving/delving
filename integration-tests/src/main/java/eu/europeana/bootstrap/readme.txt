This is a copy from Core. There bootstrap is placed into test, not main.
It cannot be moved to main, as this would require servlet container to appear on the classpath and in the war
to confuse tomcat. Separating into a separate module bootstrap is difficult because 
there is a cycle dependency with core....